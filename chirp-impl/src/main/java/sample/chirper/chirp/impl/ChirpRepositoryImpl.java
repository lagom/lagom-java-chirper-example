package sample.chirper.chirp.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.chirper.chirp.api.Chirp;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;
import static java.util.Comparator.comparing;

class ChirpRepositoryImpl implements ChirpRepository {
    private static final int NUM_RECENT_CHIRPS = 10;
    private static final String SELECT_HISTORICAL_CHIRPS =
            "SELECT * FROM chirp WHERE userId = ? AND timestamp >= ? ORDER BY timestamp ASC";
    private static final String SELECT_RECENT_CHIRPS =
            "SELECT * FROM chirp WHERE userId = ? ORDER BY timestamp DESC LIMIT ?";

    private static final Collector<Chirp, ?, TreePVector<Chirp>> pSequenceCollector =
            Collectors.collectingAndThen(Collectors.toList(), TreePVector::from);

    private final CassandraSession db;

    @Inject
    ChirpRepositoryImpl(CassandraSession db, ReadSide readSide) {
        this.db = db;
        readSide.register(ChirpTimelineEventReadSideProcessor.class);
    }

    public Source<Chirp, ?> getHistoricalChirps(PSequence<String> userIds, long timestamp) {
        List<Source<Chirp, ?>> sources = new ArrayList<>();
        for (String userId : userIds) {
            sources.add(getHistoricalChirps(userId, timestamp));
        }
        // Chirps from one user are ordered by timestamp, but chirps from different
        // users are not ordered. That can be improved by implementing a smarter
        // merge that takes the timestamps into account.
        return Source.from(sources).flatMapMerge(sources.size(), s -> s);
    }

    private Source<Chirp, NotUsed> getHistoricalChirps(String userId, long timestamp) {
        return db.select(SELECT_HISTORICAL_CHIRPS, userId, timestamp)
                .map(this::mapChirp);
    }

    public CompletionStage<PSequence<Chirp>> getRecentChirps(PSequence<String> userIds) {
        CompletionStage<PSequence<Chirp>> results = CompletableFuture.completedFuture(TreePVector.empty());
        for (String userId : userIds) {
            results = results.thenCombine(getRecentChirps(userId), PSequence::plusAll);
        }

        return results.thenApply(this::limitRecentChirps);
    }

    private PSequence<Chirp> limitRecentChirps(PSequence<Chirp> all) {
        List<Chirp> limited = all.stream()
                .sorted(comparing((Chirp chirp) -> chirp.timestamp).reversed())
                .limit(NUM_RECENT_CHIRPS)
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.reverse(limited);
        return TreePVector.from(limited);
    }

    private CompletionStage<PSequence<Chirp>> getRecentChirps(String userId) {
        return db.selectAll(SELECT_RECENT_CHIRPS, userId, NUM_RECENT_CHIRPS)
                .thenApply(this::mapChirps);
    }

    private TreePVector<Chirp> mapChirps(List<Row> chirps) {
        return chirps.stream()
                .map(this::mapChirp)
                .collect(pSequenceCollector);
    }

    private Chirp mapChirp(Row row) {
        return new Chirp(
                row.getString("userId"),
                row.getString("message"),
                Instant.ofEpochMilli(row.getLong("timestamp")),
                row.getString("uuid")
        );
    }


    private static class ChirpTimelineEventReadSideProcessor extends ReadSideProcessor<ChirpTimelineEvent> {
        private final CassandraSession db;
        private final CassandraReadSide readSide;

        private PreparedStatement insertChirp;

        @Inject
        private ChirpTimelineEventReadSideProcessor(CassandraSession db, CassandraReadSide readSide) {
            this.db = db;
            this.readSide = readSide;
        }

        @Override
        public ReadSideHandler<ChirpTimelineEvent> buildHandler() {
            return readSide.<ChirpTimelineEvent>builder("ChirpTimelineEventReadSideProcessor")
                    .setGlobalPrepare(this::createTable)
                    .setPrepare(tag -> prepareInsertChirp())
                    .setEventHandler(ChirpTimelineEvent.ChirpAdded.class,
                            event -> insertChirp(event.chirp))
                    .build();
        }

        @Override
        public PSequence<AggregateEventTag<ChirpTimelineEvent>> aggregateTags() {
            return ChirpTimelineEvent.TAG.allTags();
        }

        private CompletionStage<Done> createTable() {
            return db.executeCreateTable(
                    "CREATE TABLE IF NOT EXISTS chirp ("
                            + "userId text, timestamp bigint, uuid text, message text, "
                            + "PRIMARY KEY (userId, timestamp, uuid))");
        }

        private CompletionStage<Done> prepareInsertChirp() {
            return db.prepare("INSERT INTO chirp (userId, uuid, timestamp, message) VALUES (?, ?, ?, ?)")
                    .thenApply(s -> {
                        insertChirp = s;
                        return Done.getInstance();
                    });
        }

        private CompletionStage<List<BoundStatement>> insertChirp(Chirp chirp) {
            return completedStatement(
                    insertChirp.bind(
                            chirp.userId,
                            chirp.uuid,
                            chirp.timestamp.toEpochMilli(),
                            chirp.message
                    )
            );
        }
    }
}
