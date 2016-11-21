package sample.chirper.chirp.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import sample.chirper.chirp.api.Chirp;

import javax.annotation.Nonnull;
import java.util.Objects;

interface ChirpTimelineEvent extends Jsonable, AggregateEvent<ChirpTimelineEvent> {
    int NUM_SHARDS = 3;

    AggregateEventShards<ChirpTimelineEvent> TAG = AggregateEventTag.sharded(ChirpTimelineEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<ChirpTimelineEvent> aggregateTag() {
        return TAG;
    }

    final class ChirpAdded implements ChirpTimelineEvent {
        @Nonnull
        final Chirp chirp;

        @JsonCreator
        ChirpAdded(@Nonnull Chirp chirp) {
            this.chirp = Objects.requireNonNull(chirp, "chirp");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChirpAdded that = (ChirpAdded) o;

            return chirp.equals(that.chirp);
        }

        @Override
        public int hashCode() {
            return chirp.hashCode();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("chirp", chirp)
                    .toString();
        }
    }
}
