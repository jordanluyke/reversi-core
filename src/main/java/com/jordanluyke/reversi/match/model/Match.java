package com.jordanluyke.reversi.match.model;

import com.jordanluyke.reversi.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Match {

    private String id = RandomUtil.generateRandom(12);
//    private String playerLightId;
//    private String playerDarkId;
    private String playerLightId = RandomUtil.generateRandom(12);
    private String playerDarkId = RandomUtil.generateRandom(12);
    private Side turn = Side.DARK;
    private Board board = Board.create();

    public Observable<Match> placePiece(Side side, Position position) {
        return board.placePiece(side, position)
                .map(Void -> this);
    }
}
