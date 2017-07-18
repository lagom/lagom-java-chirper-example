/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.activity.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.testkit.ServiceTest.Setup;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.chirper.activity.api.ActivityStreamService;
import sample.chirper.chirp.api.*;
import sample.chirper.friend.api.*;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import akka.stream.testkit.TestSubscriber.Probe;
import akka.stream.testkit.javadsl.TestSink;

public class ActivityStreamServiceTest {

  private final Setup setup = defaultSetup().withCluster(false)
      .configureBuilder(b -> b.overrides(bind(FriendService.class).to(FriendServiceStub.class),
          bind(ChirpService.class).to(ChirpServiceStub.class)));

  @Test
  public void shouldGetLiveFeed() throws Exception {
    withServer(setup, server -> {
      ActivityStreamService feedService = server.client(ActivityStreamService.class);
      Source<Chirp, ?> chirps = feedService.getLiveActivityStream("usr1").invoke()
          .toCompletableFuture().get(3, SECONDS);
      Probe<Chirp> probe = chirps.runWith(TestSink.probe(server.system()), server.materializer());
      probe.request(10);
      assertEquals("msg1", probe.expectNext().message);
      assertEquals("msg2", probe.expectNext().message);
      probe.cancel();
    });
  }

  @Test
  public void shouldGetHistoricalFeed() throws Exception {
    withServer(setup, server -> {
      ActivityStreamService feedService = server.client(ActivityStreamService.class);
      Source<Chirp, ?> chirps = feedService.getHistoricalActivityStream("usr1").invoke()
          .toCompletableFuture().get(3, SECONDS);
      Probe<Chirp> probe = chirps.runWith(TestSink.probe(server.system()), server.materializer());
      probe.request(10);
      assertEquals("msg1", probe.expectNext().message);
      probe.expectComplete();
    });
  }



  static class FriendServiceStub implements FriendService {

    private final User usr1 = new User("usr1", "User 1", 
        Optional.of(TreePVector.<String>empty().plus("usr2")));
    private final User usr2 = new User("usr2", "User 2");

    @Override
    public ServiceCall<NotUsed, User> getUser(String userId) {
      return req -> {
        if (userId.equals(usr1.userId))
          return completedFuture(usr1);
        else if (userId.equals(usr2.userId))
          return completedFuture(usr2);
        else
          throw new NotFound(userId);
      };
    }

    @Override
    public ServiceCall<User, NotUsed> createUser() {
      return req -> completedFuture(NotUsed.getInstance());
    }

    @Override
    public ServiceCall<FriendId, NotUsed> addFriend(String userId) {
      return req -> completedFuture(NotUsed.getInstance());
    }

    @Override
    public ServiceCall<NotUsed, PSequence<String>> getFollowers(String userId) {
      return req -> {
        if (userId.equals(usr1.userId))
          return completedFuture(TreePVector.<String>empty());
        else if (userId.equals(usr2.userId))
          return completedFuture(TreePVector.<String>empty().plus("usr1"));
        else
          throw new NotFound(userId);
      };
    }
  }

  static class ChirpServiceStub implements ChirpService {

    @Override
    public ServiceCall<Chirp, NotUsed> addChirp(String userId) {
      return req -> completedFuture(NotUsed.getInstance());
    }

    @Override
    public ServiceCall<LiveChirpsRequest, Source<Chirp, ?>> getLiveChirps() {
      return req -> {
        if (req.userIds.contains("usr2")) {
          Chirp c1 = new Chirp("usr2", "msg1");
          Chirp c2 = new Chirp("usr2", "msg2");
          return completedFuture(Source.from(Arrays.asList(c1, c2)));
        } else
          return completedFuture(Source.empty());
      };
    }

    @Override
    public ServiceCall<HistoricalChirpsRequest, Source<Chirp, ?>> getHistoricalChirps() {
      return req -> {
        if (req.userIds.contains("usr2")) {
          Chirp c1 = new Chirp("usr2", "msg1");
          return completedFuture(Source.single(c1));
        } else
          return completedFuture(Source.empty());
      };
    }

  }
}
