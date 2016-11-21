package sample.chirper.chirp.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.datastax.driver.core.Row;
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
    ChirpRepositoryImpl(CassandraSession db) {
        this.db = db;
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
}
