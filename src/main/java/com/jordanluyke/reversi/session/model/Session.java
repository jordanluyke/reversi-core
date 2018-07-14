package com.jordanluyke.reversi.session.model;

import lombok.*;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Session {

    private String id;
    private Instant createdAt;
    private String ownerId;
    private Optional<Instant> expiresAt;
}
