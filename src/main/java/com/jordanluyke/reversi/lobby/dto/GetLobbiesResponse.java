package com.jordanluyke.reversi.lobby.dto;

import com.jordanluyke.reversi.lobby.model.Lobby;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetLobbiesResponse {
    List<Lobby> lobbies;
}
