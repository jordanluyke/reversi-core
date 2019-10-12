package com.jordanluyke.reversi.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLobbyRequest {
    private String accountId;
    private Optional<String> name = Optional.empty();
}
