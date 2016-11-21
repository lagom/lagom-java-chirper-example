/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import play.Logger;
import play.Logger.ALogger;
import sample.chirper.chirp.api.Chirp;
import sample.chirper.chirp.api.ChirpService;
import sample.chirper.chirp.api.HistoricalChirpsRequest;
import sample.chirper.chirp.api.LiveChirpsRequest;
import sample.chirper.chirp.impl.ChirpTimelineCommand.AddChirp;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ChirpServiceImpl implements ChirpService {
    private final PersistentEntityRegistry persistentEntitites;
    private final ChirpTopic topic;
    private final ChirpRepository chirps;
    private final CassandraSession db;
    private final ALogger log = Logger.of(getClass());

    @Inject
    public ChirpServiceImpl(PersistentEntityRegistry persistentEntitites, ChirpTopic topic, ChirpRepository chirps, CassandraSession db) {
        this.persistentEntitites = persistentEntitites;
        this.topic = topic;
        this.chirps = chirps;
        this.db = db;

        persistentEntitites.register(ChirpTimelineEntity.class);

        createTable();
    }

    private void createTable() {
        // @formatter:off
        CompletionStage<Done> result = db.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS chirp ("
                        + "userId text, timestamp bigint, uuid text, message text, "
                        + "PRIMARY KEY (userId, timestamp, uuid))");
        // @formatter:on
        result.whenComplete((ok, err) -> {
            if (err != null) {
                log.error("Failed to create chirp table, due to: " + err.getMessage(), err);
            }
        });
    }

    @Override
    public ServiceCall<Chirp, NotUsed> addChirp(String userId) {
        return chirp -> {
            if (!userId.equals(chirp.userId))
                throw new IllegalArgumentException("UserId " + userId + " did not match userId in " + chirp);

            return persistentEntitites.refFor(ChirpTimelineEntity.class, userId)
                    .ask(new AddChirp(chirp))
                    .thenCompose((ack) ->

                            db.executeWrite("INSERT INTO chirp (userId, uuid, timestamp, message) VALUES (?, ?, ?, ?)",
                                    chirp.userId, chirp.uuid, chirp.timestamp.toEpochMilli(),
                                    chirp.message).thenApply(done -> NotUsed.getInstance())
                    );
        };
    }

    @Override
    public ServiceCall<LiveChirpsRequest, Source<Chirp, ?>> getLiveChirps() {
        return req -> chirps.getRecentChirps(req.userIds).thenApply(recentChirps -> {
            List<Source<Chirp, ?>> sources = new ArrayList<>();
            for (String userId : req.userIds) {
                sources.add(topic.subscriber(userId));
            }
            HashSet<String> users = new HashSet<>(req.userIds);
            Source<Chirp, ?> publishedChirps = Source.from(sources).flatMapMerge(sources.size(), s -> s)
                    .filter(c -> users.contains(c.userId));

            // We currently ignore the fact that it is possible to get duplicate chirps
            // from the recent and the topic. That can be solved with a de-duplication stage.
            return Source.from(recentChirps).concat(publishedChirps);
        });
    }

    @Override
    public ServiceCall<HistoricalChirpsRequest, Source<Chirp, ?>> getHistoricalChirps() {
        return req -> {
            PSequence<String> userIds = req.userIds;
            long timestamp = req.fromTime.toEpochMilli();
            Source<Chirp, ?> result = chirps.getHistoricalChirps(userIds, timestamp);
            return CompletableFuture.completedFuture(result);
        };
    }
}
