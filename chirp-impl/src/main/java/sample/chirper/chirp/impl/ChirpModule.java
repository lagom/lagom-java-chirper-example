/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import sample.chirper.chirp.api.ChirpService;

public class ChirpModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(ChirpService.class, ChirpServiceImpl.class);
        bind(ChirpTopic.class).to(ChirpTopicImpl.class);
        bind(ChirpRepository.class).to(ChirpRepositoryImpl.class);
    }
}
