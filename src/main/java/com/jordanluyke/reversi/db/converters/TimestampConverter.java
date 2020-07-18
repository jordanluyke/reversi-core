package com.jordanluyke.reversi.db.converters;

import org.jooq.impl.AbstractConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;


/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class TimestampConverter extends AbstractConverter<LocalDateTime, Instant> {
    public static final long serialVersionUID = 102L;

    public TimestampConverter() {
        super(LocalDateTime.class, Instant.class);
    }

    @Override
    public Instant from(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime to(Instant instant) {
        return instant == null ? null : instant.atZone(ZoneId.of("UTC")).toLocalDateTime();
    }
}
