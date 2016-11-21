package sample.chirper.chirp.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import sample.chirper.chirp.api.Chirp;

/**
 * Allows you to publish chirps to internal subscribers within this service.
 */
interface ChirpTopic {
    /**
     * Publishes the provided chirp to subscribers based on the user ID within the chirp.
     *
     * @param chirp the chirp to publish
     */
    void publish(Chirp chirp);

    /**
     * Returns a source of chirps published by the provided user ID.
     *
     * @param userId the ID of the user whose timeline the caller is subscribing to
     * @return a continuous source of chirps for the provided user
     */
    Source<Chirp, NotUsed> subscriber(String userId);
}
