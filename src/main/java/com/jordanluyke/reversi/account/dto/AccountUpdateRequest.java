package com.jordanluyke.reversi.account.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class AccountUpdateRequest {
    private Optional<String> name = Optional.empty();
}
