package sample.chirper.chirp.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import sample.chirper.chirp.api.Chirp;

import javax.inject.Inject;

class ChirpTopicImpl implements ChirpTopic {
    private static final int MAX_TOPICS = 1024;

    private final PubSubRegistry pubSub;

    @Inject
    public ChirpTopicImpl(PubSubRegistry pubSub) {
        this.pubSub = pubSub;
    }

    @Override
    public void publish(Chirp chirp) {
        refFor(chirp.userId).publish(chirp);
    }

    @Override
    public Source<Chirp, NotUsed> subscriber(String userId) {
        return refFor(userId).subscriber();
    }

    private PubSubRef<Chirp> refFor(String userId) {
        return pubSub.refFor(TopicId.of(Chirp.class, topicQualifier(userId)));
    }

    private String topicQualifier(String userId) {
        return String.valueOf(Math.abs(userId.hashCode()) % MAX_TOPICS);
    }
}
