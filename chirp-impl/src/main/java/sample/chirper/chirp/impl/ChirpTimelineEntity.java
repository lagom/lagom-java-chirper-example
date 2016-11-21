package sample.chirper.chirp.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import sample.chirper.chirp.impl.ChirpTimelineCommand.AddChirp;
import sample.chirper.chirp.impl.ChirpTimelineEvent.ChirpAdded;

import javax.inject.Inject;
import java.util.Optional;

class ChirpTimelineEntity extends PersistentEntity<ChirpTimelineCommand, ChirpTimelineEvent, NotUsed> {
    private final ChirpTopic topic;

    @Inject
    ChirpTimelineEntity(ChirpTopic topic) {
        this.topic = topic;
    }

    @Override
    public Behavior initialBehavior(Optional<NotUsed> snapshotState) {
        BehaviorBuilder b = newBehaviorBuilder(NotUsed.getInstance());
        b.setCommandHandler(AddChirp.class, this::addChirp);
        b.setEventHandler(ChirpAdded.class, evt -> state());
        return b.build();
    }

    private Persist addChirp(AddChirp cmd, CommandContext<Done> ctx) {
        return ctx.thenPersist(new ChirpAdded(cmd.chirp), evt -> {
            ctx.reply(Done.getInstance());
            topic.publish(cmd.chirp);
        });
    }
}
