/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import com.google.inject.AbstractModule;
import sample.chirper.friend.api.FriendService;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class FriendModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(FriendService.class, FriendServiceImpl.class);
  }
}
