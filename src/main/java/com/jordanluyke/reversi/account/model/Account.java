package com.jordanluyke.reversi.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.jooq.sources.tables.records.AccountRecord;

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
public class Account {

    private String id;
    private Instant createdAt;
    @JsonIgnore private Instant updatedAt;
    private Optional<String> name;
    private Optional<String> facebookUserId;
    private Optional<String> googleUserId;
    private boolean guest;

    public static Account fromRecord(AccountRecord record) {
        return new Account(record.getId(), record.getCreatedat(), record.getUpdatedat(), Optional.ofNullable(record.getName()), Optional.ofNullable(record.getFacebookuserid()), Optional.ofNullable(record.getGoogleuserid()), record.getGuest());
    }
}
