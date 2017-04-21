/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceAcl;
import com.lightbend.lagom.javadsl.api.ServiceInfo;
import com.lightbend.lagom.javadsl.client.ServiceClientGuiceSupport;

public class Module extends AbstractModule implements ServiceClientGuiceSupport {
    @Override
    protected void configure() {
        bindServiceInfo(ServiceInfo.of(
                "chirper-front-end",
                ServiceAcl.path("(?!/api/).*")
        ));
    }
}
