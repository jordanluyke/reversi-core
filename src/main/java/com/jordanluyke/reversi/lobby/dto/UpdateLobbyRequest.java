package com.jordanluyke.reversi.lobby.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class UpdateLobbyRequest {
    public Optional<String> playerIdLight = Optional.empty();
    public Optional<Boolean> playerDarkReady = Optional.empty();
    public Optional<Boolean> playerLightReady = Optional.empty();
}
