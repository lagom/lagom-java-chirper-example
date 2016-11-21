package sample.chirper.chirp.impl;

import akka.stream.javadsl.Source;
import org.pcollections.PSequence;
import sample.chirper.chirp.api.Chirp;

import java.util.concurrent.CompletionStage;

/**
 * Provides access to past chirps. See {@link ChirpTopic} for real-time access to new chirps.
 */
interface ChirpRepository {
    Source<Chirp, ?> getHistoricalChirps(PSequence<String> userIds, long timestamp);

    CompletionStage<PSequence<Chirp>> getRecentChirps(PSequence<String> userIds);
}
