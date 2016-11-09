/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.util.List;
import java.util.concurrent.CompletionStage;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.Done;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.chirper.friend.impl.FriendEvent.FriendAdded;

import javax.inject.Inject;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;

public class FriendEventProcessor extends ReadSideProcessor<FriendEvent> {

  private final CassandraSession session;
  private final CassandraReadSide readSide;

  private PreparedStatement writeFollowers = null; // initialized in prepare

  @Inject
  public FriendEventProcessor(CassandraSession session, CassandraReadSide readSide) {
    this.session = session;
    this.readSide = readSide;
  }

  private void setWriteFollowers(PreparedStatement writeFollowers) {
    this.writeFollowers = writeFollowers;
  }

  @Override
  public PSequence<AggregateEventTag<FriendEvent>> aggregateTags() {
    return TreePVector.singleton(FriendEventTag.INSTANCE);
  }

  @Override
  public ReadSideHandler<FriendEvent> buildHandler() {
    return readSide.<FriendEvent>builder("friend_offset")
            .setGlobalPrepare(this::prepareCreateTables)
            .setPrepare((ignored) -> prepareWriteFollowers())
            .setEventHandler(FriendAdded.class, this::processFriendChanged)
            .build();
  }

  private CompletionStage<Done> prepareCreateTables() {
    // @formatter:off
    return session.executeCreateTable(
        "CREATE TABLE IF NOT EXISTS follower ("
          + "userId text, followedBy text, "
          + "PRIMARY KEY (userId, followedBy))");
    // @formatter:on
  }

  private CompletionStage<Done> prepareWriteFollowers() {
    return session.prepare("INSERT INTO follower (userId, followedBy) VALUES (?, ?)").thenApply(ps -> {
      setWriteFollowers(ps);
      return Done.getInstance();
    });
  }

  private CompletionStage<List<BoundStatement>> processFriendChanged(FriendAdded event) {
    BoundStatement bindWriteFollowers = writeFollowers.bind();
    bindWriteFollowers.setString("userId", event.friendId);
    bindWriteFollowers.setString("followedBy", event.userId);
    return completedStatement(bindWriteFollowers);
  }

}
