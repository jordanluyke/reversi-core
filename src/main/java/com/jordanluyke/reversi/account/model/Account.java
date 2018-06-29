package com.jordanluyke.reversi.account.model;

import lombok.*;
import org.jooq.sources.tables.records.AccountRecord;

import java.util.Date;

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
    private Date createdAt;
    private String email;

    public static Account fromRecord(AccountRecord record) {
        return new Account(record.getId(), record.getCreatedat(), record.getEmail());
    }
}
