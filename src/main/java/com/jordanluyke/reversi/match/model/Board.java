package com.jordanluyke.reversi.match.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Board {

    private Side[] squares;
    private String transcript;

    public static Board create() {
        Side[] squares = new Side[64];
        squares[25] = Side.LIGHT;
        squares[26] = Side.DARK;
        squares[32] = Side.DARK;
        squares[33] = Side.LIGHT;
        return new Board(squares, "");
    }

    public void setSquare(Position position, Side side) {
        squares[position.getIndex()] = side;
        transcript += position.getCoordinates();
    }

    public int getAmount(Side side) {
        return Arrays.stream(squares)
                .filter(square -> square == side)
                .collect(Collectors.toList())
                .size();
    }

    public void placePiece(Side side, Position position) {
//        if(!isValidMove(side, position))
//            throw new IllegalMoveException()
    }

    private boolean isValidMove(Side side, Position position) {
        if(squares[position.getIndex()] != null)
            return false;
        return true;
    }
    // check for available legal moves (backtrack from open spaces next to pieces)
}
