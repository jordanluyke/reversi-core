package com.jordanluyke.reversi.db.converters;

import org.jooq.Converter;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class TimestampConverter implements Converter<Timestamp, Instant> {

    @Override
    public Instant from(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    @Override
    public Timestamp to(Instant instant) {
        return instant == null ? null : new Timestamp(instant.toEpochMilli());
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
