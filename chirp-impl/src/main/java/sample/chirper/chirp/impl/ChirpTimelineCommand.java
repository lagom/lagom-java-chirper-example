package sample.chirper.chirp.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import jdk.nashorn.internal.ir.annotations.Immutable;
import sample.chirper.chirp.api.Chirp;

import javax.annotation.Nonnull;
import java.util.Objects;

interface ChirpTimelineCommand extends Jsonable {
    @Immutable
    final class AddChirp implements ChirpTimelineCommand, ReplyType<Done> {
        @Nonnull
        final Chirp chirp;

        @JsonCreator
        AddChirp(@Nonnull Chirp chirp) {
            this.chirp = Objects.requireNonNull(chirp, "chirp");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AddChirp addChirp = (AddChirp) o;

            return chirp.equals(addChirp.chirp);
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
