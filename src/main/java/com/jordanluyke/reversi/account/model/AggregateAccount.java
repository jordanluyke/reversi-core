package com.jordanluyke.reversi.account.model;

import lombok.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class AggregateAccount extends Account {
    private static final Logger logger = LogManager.getLogger(AggregateAccount.class);

    private PlayerStats stats;

    public AggregateAccount(Account account, PlayerStats stats) {
        try {
            BeanUtils.copyProperties(this, account);
        } catch(IllegalAccessException | InvocationTargetException e) {
            logger.error("BeanUtils error: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        this.stats = stats;
    }
}
