package sample.chirper.chirp.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import sample.chirper.chirp.api.Chirp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static sample.chirper.chirp.impl.ChirpTimelineCommand.AddChirp;

public class ChirpTimelineEntityTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("ChirpTimelineEntityTest");
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAddChirp() {
        ChirpTopicStub topic = new ChirpTopicStub();
        PersistentEntityTestDriver<ChirpTimelineCommand, ChirpTimelineEvent, NotUsed> driver =
                new PersistentEntityTestDriver<>(system, new ChirpTimelineEntity(topic), "user-1");

        Chirp chirp = new Chirp("user-1", "Hello, world");

        PersistentEntityTestDriver.Outcome<ChirpTimelineEvent, NotUsed> outcome =
                driver.run(new AddChirp(chirp));
        assertEquals(Done.getInstance(), outcome.getReplies().get(0));
        assertEquals(chirp, ((ChirpTimelineEvent.ChirpAdded) outcome.events().get(0)).chirp);
        assertEquals(chirp, topic.chirps.get(0));
        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    static class ChirpTopicStub implements ChirpTopic {
        final List<Chirp> chirps = new ArrayList<>();

        @Override
        public void publish(Chirp chirp) {
            chirps.add(chirp);
        }

        @Override
        public Source<Chirp, NotUsed> subscriber(String userId) {
            return Source.from(chirps);
        }
    }
}
