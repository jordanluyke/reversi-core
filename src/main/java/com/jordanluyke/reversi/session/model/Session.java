package com.jordanluyke.reversi.session.model;

import lombok.*;

import java.util.Date;
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
    private Date createdAt;
    private String ownerId;
    private Optional<Date> expiresAt;
}
