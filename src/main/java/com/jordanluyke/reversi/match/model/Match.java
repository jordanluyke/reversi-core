package com.jordanluyke.reversi.match.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Match {

    private String id;
    private String playerLightId;
    private String playerDarkId;
    private Side turn = Side.LIGHT;
    private Board board = new Board();
}
