package com.jordanluyke.reversi.account.model;

import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AggregateAccount {
    private static final Logger logger = LogManager.getLogger(AggregateAccount.class);

    private Account account;
    private PlayerStats stats;
}
