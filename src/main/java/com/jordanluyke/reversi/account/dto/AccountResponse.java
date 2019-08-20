package com.jordanluyke.reversi.account.dto;

import com.jordanluyke.reversi.account.model.Account;
import com.jordanluyke.reversi.account.model.AggregateAccount;
import com.jordanluyke.reversi.account.model.PlayerStats;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class AccountResponse extends Account {

    private PlayerStats stats;

    public static AccountResponse fromAggregateAccount(AggregateAccount aggregateAccount) {
        AccountResponse res = new AccountResponse();
        Account account = aggregateAccount.getAccount();
        res.setId(account.getId());
        res.setCreatedAt(account.getCreatedAt());
        res.setUpdatedAt(account.getUpdatedAt());
        res.setName(account.getName());
        res.setFacebookUserId(account.getFacebookUserId());
        res.setGoogleUserId(account.getGoogleUserId());
        res.setGuest(account.isGuest());
        res.setStats(aggregateAccount.getStats());
        return res;
    }
}
