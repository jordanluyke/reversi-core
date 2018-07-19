package com.jordanluyke.reversi.session.dto;

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
public class SessionResponse {
    private String accountId;
    private String sessionId;
    private Optional<Instant> expiresAt;
}
