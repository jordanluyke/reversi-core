package com.jordanluyke.reversi.accounts.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class AccountCreationRequest {

    private String email;
    private String password;
}
