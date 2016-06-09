/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.namedCall;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

/**
 * The friend service.
 */
public interface FriendService extends Service {

  /**
   * Service call for getting a user.
   *
   * The ID of this service call is the user name, and the response message is the User object.
   */
  ServiceCall<NotUsed, User> getUser(String userId);

  /**
   * Service call for creating a user.
   *
   * The request message is the User to create.
   */
  ServiceCall<User, NotUsed> createUser();

  /**
   * Service call for adding a friend to a user.
   *
   * The ID for this service call is the ID of the user that the friend is being added to.
   * The request message is the ID of the friend being added.
   */
  ServiceCall<FriendId, NotUsed> addFriend(String userId);

  /**
   * Service call for getting the followers of a user.
   *
   * The ID for this service call is the Id of the user to get the followers for.
   * The response message is the list of follower IDs.
   */
  ServiceCall<NotUsed, PSequence<String>> getFollowers(String userId);

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("friendservice").withCalls(
        pathCall("/api/users/:userId", this::getUser),
        namedCall("/api/users", this::createUser),
        pathCall("/api/users/:userId/friends", this::addFriend),
        pathCall("/api/users/:userId/followers", this::getFollowers)
      ).withAutoAcl(true);
    // @formatter:on
  }
}
