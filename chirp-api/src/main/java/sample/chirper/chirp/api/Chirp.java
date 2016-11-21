/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("serial")
@Immutable
public final class Chirp implements Jsonable {
    public final String userId;
    public final String message;
    public final Instant timestamp;
    public final String uuid;

    public Chirp(String userId, String message) {
        this(userId, message, Optional.empty(), Optional.empty());
    }

    @JsonCreator
    public Chirp(String userId, String message, Optional<Instant> timestamp, Optional<String> uuid) {
        this(
                userId,
                message,
                timestamp.orElseGet(Instant::now),
                uuid.orElseGet(() -> UUID.randomUUID().toString())
        );
    }

    public Chirp(String userId, String message, Instant timestamp, String uuid) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.message = Objects.requireNonNull(message, "message");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    @Override
    public boolean equals(@Nullable Object another) {
        if (this == another)
            return true;
        return another instanceof Chirp && equalTo((Chirp) another);
    }

    private boolean equalTo(Chirp another) {
        return userId.equals(another.userId) && message.equals(another.message) && timestamp.equals(another.timestamp)
                && uuid.equals(another.uuid);
    }

    @Override
    public int hashCode() {
        int h = 31;
        h = h * 17 + userId.hashCode();
        h = h * 17 + message.hashCode();
        h = h * 17 + timestamp.hashCode();
        h = h * 17 + uuid.hashCode();
        return h;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Chirp")
                .add("userId", userId)
                .add("message", message)
                .add("timestamp", timestamp)
                .add("uuid", uuid)
                .toString();
    }
}
