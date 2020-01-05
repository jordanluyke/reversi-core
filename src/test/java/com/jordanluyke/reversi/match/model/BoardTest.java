package com.jordanluyke.reversi.match.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class BoardTest {
    private static final Logger logger = LogManager.getLogger(BoardTest.class);

//    private TestEnv env = new TestEnv();

    @Test
    public void test1() {
        Board board = new Board();

        board.setSquares(new Side[]{Side.DARK,Side.DARK,Side.DARK,Side.DARK,Side.LIGHT,null,null,null,Side.DARK,Side.LIGHT,Side.DARK,Side.DARK,Side.LIGHT,Side.LIGHT,null,null,Side.DARK,Side.DARK,Side.DARK,Side.DARK,Side.LIGHT,Side.LIGHT,null,Side.DARK,Side.DARK,Side.LIGHT,Side.DARK,Side.DARK,Side.LIGHT,Side.LIGHT,null,Side.DARK,Side.DARK,Side.LIGHT,Side.DARK,Side.DARK,Side.DARK,Side.LIGHT,Side.LIGHT,Side.DARK,Side.DARK,Side.LIGHT,Side.LIGHT,Side.DARK,Side.DARK,Side.LIGHT,Side.LIGHT,Side.DARK,Side.DARK,Side.LIGHT,Side.LIGHT,Side.LIGHT,Side.LIGHT,Side.LIGHT,Side.LIGHT,Side.DARK,Side.DARK,Side.DARK,Side.DARK,Side.DARK,Side.DARK,Side.DARK,Side.DARK,Side.DARK});
        Assertions.assertFalse(board.canPlacePiece(Side.LIGHT));

        board.setSquares(new Side[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,Side.LIGHT,Side.DARK,null,null,null,null,null,null,Side.DARK,Side.LIGHT,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null});
        Assertions.assertTrue(board.canPlacePiece(Side.LIGHT));
    }
}
