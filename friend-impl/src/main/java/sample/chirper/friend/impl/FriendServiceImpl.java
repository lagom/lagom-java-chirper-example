/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.persistence.ReadSide;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.NotUsed;
import sample.chirper.friend.api.FriendId;
import sample.chirper.friend.api.FriendService;
import sample.chirper.friend.api.User;
import sample.chirper.friend.impl.FriendCommand.AddFriend;
import sample.chirper.friend.impl.FriendCommand.CreateUser;
import sample.chirper.friend.impl.FriendCommand.GetUser;

public class FriendServiceImpl implements FriendService {

  private final PersistentEntityRegistry persistentEntities;
  private final CassandraSession db;

  @Inject
  public FriendServiceImpl(PersistentEntityRegistry persistentEntities, ReadSide readSide,
      CassandraSession db) {
    this.persistentEntities = persistentEntities;
    this.db = db;

    persistentEntities.register(FriendEntity.class);
    readSide.register(FriendEventProcessor.class);
  }

  @Override
  public ServiceCall<NotUsed, User> getUser(String userId) {
    return request -> {
      return friendEntityRef(userId).ask(new GetUser()).thenApply(reply -> {
        if (reply.user.isPresent())
          return reply.user.get();
        else
          throw new NotFound("user " + userId + " not found");
      });
    };
  }

  @Override
  public ServiceCall<User, NotUsed> createUser() {
    return request -> {
      return friendEntityRef(request.userId).ask(new CreateUser(request))
          .thenApply(ack -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<FriendId, NotUsed> addFriend(String userId) {
    return request -> {
      return friendEntityRef(userId).ask(new AddFriend(request.friendId))
          .thenApply(ack -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<NotUsed, PSequence<String>> getFollowers(String userId) {
    return req -> {
      CompletionStage<PSequence<String>> result = db.selectAll("SELECT * FROM follower WHERE userId = ?", userId)
        .thenApply(rows -> {
        List<String> followers = rows.stream().map(row -> row.getString("followedBy")).collect(Collectors.toList());
        return TreePVector.from(followers);
      });
      return result;
    };
  }

  private PersistentEntityRef<FriendCommand> friendEntityRef(String userId) {
    PersistentEntityRef<FriendCommand> ref = persistentEntities.refFor(FriendEntity.class, userId);
    return ref;
  }

}
