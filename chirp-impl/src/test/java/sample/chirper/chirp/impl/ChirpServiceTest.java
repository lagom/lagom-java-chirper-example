/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.impl;

import akka.stream.javadsl.Source;
import akka.stream.testkit.TestSubscriber.Probe;
import akka.stream.testkit.javadsl.TestSink;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.pcollections.TreePVector;
import sample.chirper.chirp.api.Chirp;
import sample.chirper.chirp.api.ChirpService;
import sample.chirper.chirp.api.HistoricalChirpsRequest;
import sample.chirper.chirp.api.LiveChirpsRequest;
import scala.concurrent.duration.FiniteDuration;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChirpServiceTest {

    private static TestServer server;

    // Creating a new persistent entity is slow in some runtime environment (e.g. free tier Travis) causing the
    // tests to fail on timeout. First time we invoke a call that requires a persistent entity we will
    // wait a bit longer.
    private static int persistentEntityDefault;
    private static final int probeRequestTimeout = 10;
    private static final int serviceInvocationTimeout = 3;

    @BeforeClass
    public static void setUp() throws InterruptedException, ExecutionException, TimeoutException {
        server = startServer(defaultSetup().withCassandra(true));

        // This is a canary wait to ensure the server is up and running so tests can start. If
        // this fails it's not a test failure, it's that the machine running this is slow.
        ChirpService chirpService = server.client(ChirpService.class);
        LiveChirpsRequest request = new LiveChirpsRequest(TreePVector.<String>empty().plus("usr1").plus("usr2"));
        chirpService.getLiveChirps().invoke(request).toCompletableFuture().get(10, SECONDS);

        // Let's run the tests with timeouts equivalent to production settings _plus_ one second.
        persistentEntityDefault = 1 + (int) server.app().config().getDuration("lagom.persistence.ask-timeout", SECONDS);
    }

    @AfterClass
    public static void tearDown() {
        server.stop();
        server = null;
    }

    @Test
    public void shouldPublishChirpsToSubscribers() throws Exception {
        ChirpService chirpService = server.client(ChirpService.class);
        LiveChirpsRequest request = new LiveChirpsRequest(TreePVector.<String>empty().plus("usr1").plus("usr2"));
        Source<Chirp, ?> chirps1 = chirpService.getLiveChirps().invoke(request).toCompletableFuture().get(serviceInvocationTimeout, SECONDS);
        Probe<Chirp> probe1 = chirps1.runWith(TestSink.probe(server.system()), server.materializer());
        probe1.request(probeRequestTimeout);
        Source<Chirp, ?> chirps2 = chirpService.getLiveChirps().invoke(request).toCompletableFuture().get(serviceInvocationTimeout, SECONDS);
        Probe<Chirp> probe2 = chirps2.runWith(TestSink.probe(server.system()), server.materializer());
        probe2.request(probeRequestTimeout);

        Chirp chirp1 = new Chirp("usr1", "hello 1");
        addChirp(chirpService, chirp1);
        probe1.expectNext(chirp1);
        probe2.expectNext(chirp1);

        Chirp chirp2 = new Chirp("usr1", "hello 2");
        addChirp(chirpService, chirp2);
        probe1.expectNext(chirp2);
        probe2.expectNext(chirp2);

        Chirp chirp3 = new Chirp("usr2", "hello 3");
        addChirp(chirpService, chirp3);
        probe1.expectNext(chirp3);
        probe2.expectNext(chirp3);

        probe1.cancel();
        probe2.cancel();
    }

    @Test
    public void shouldIncludeSomeOldChirpsInLiveFeed() throws Exception {
        ChirpService chirpService = server.client(ChirpService.class);

        Chirp chirp1 = new Chirp("usr3", "hi 1");
        addChirp(chirpService, chirp1);

        Chirp chirp2 = new Chirp("usr4", "hi 2");
        addChirp(chirpService, chirp2);

        LiveChirpsRequest request = new LiveChirpsRequest(TreePVector.<String>empty().plus("usr3").plus("usr4"));

        eventually(FiniteDuration.create(10, SECONDS), () -> {
            Source<Chirp, ?> chirps = chirpService.getLiveChirps().invoke(request).toCompletableFuture().get(serviceInvocationTimeout, SECONDS);
            Probe<Chirp> probe = chirps.runWith(TestSink.probe(server.system()), server.materializer());
            probe.request(probeRequestTimeout);
            probe.expectNextUnordered(chirp1, chirp2);

            Chirp chirp3 = new Chirp("usr4", "hi 3");
            addChirp(chirpService, chirp3);
            probe.expectNext(chirp3);

            probe.cancel();
        });
    }

    @Test
    public void shouldRetrieveOldChirps() throws Exception {
        ChirpService chirpService = server.client(ChirpService.class);

        Chirp chirp1 = new Chirp("usr5", "msg 1");
        addChirp(chirpService, chirp1);

        Chirp chirp2 = new Chirp("usr6", "msg 2");
        addChirp(chirpService, chirp2);

        HistoricalChirpsRequest request = new HistoricalChirpsRequest(Instant.now().minusSeconds(20),
            TreePVector.<String>empty().plus("usr5").plus("usr6"));

        eventually(FiniteDuration.create(10, SECONDS), () -> {
            Source<Chirp, ?> chirps = chirpService.getHistoricalChirps().invoke(request).toCompletableFuture().get(serviceInvocationTimeout, SECONDS);
            Probe<Chirp> probe = chirps.runWith(TestSink.probe(server.system()), server.materializer());
            probe.request(probeRequestTimeout);
            probe.expectNextUnordered(chirp1, chirp2);
            probe.expectComplete();
        });
    }

    private void addChirp(ChirpService chirpService, Chirp chirp) throws InterruptedException, ExecutionException, TimeoutException {
        chirpService.addChirp(chirp.userId).invoke(chirp).toCompletableFuture().get(persistentEntityDefault, SECONDS);
    }
}
