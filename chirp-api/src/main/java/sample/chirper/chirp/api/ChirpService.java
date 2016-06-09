/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.api;

import akka.stream.javadsl.Source;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import static com.lightbend.lagom.javadsl.api.Service.*;

public interface ChirpService extends Service {

  ServiceCall<Chirp, NotUsed> addChirp(String userId);
  
  ServiceCall<LiveChirpsRequest, Source<Chirp, ?>> getLiveChirps();
  
  ServiceCall<HistoricalChirpsRequest, Source<Chirp, ?>> getHistoricalChirps();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("chirpservice").withCalls(
        pathCall("/api/chirps/live/:userId", this::addChirp),
        namedCall("/api/chirps/live", this::getLiveChirps),
        namedCall("/api/chirps/history", this::getHistoricalChirps)
      ).withAutoAcl(true);
    // @formatter:on
  }
}
