package com.jordanluyke.reversi.accounts.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.sources.tables.records.AccountRecord;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private String id;
    private String email;
    @JsonIgnore private String password;

    public static Account fromRecord(AccountRecord record) {
        return new Account(record.getId(), record.getEmail(), record.getPassword());
    }
}
