package com.jordanluyke.reversi.match.model;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public enum Side {
    LIGHT,
    DARK;

    public Side getOpposite() {
        return this == LIGHT ? DARK : LIGHT;
    }
}
