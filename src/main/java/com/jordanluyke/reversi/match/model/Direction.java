package com.jordanluyke.reversi.match.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@AllArgsConstructor
public enum Direction {
    NORTH(1, 0),
    NORTHEAST(1, 1),
    EAST(0, 1),
    SOUTHEAST(-1, 1),
    SOUTH(-1, 0),
    SOUTHWEST(-1, -1),
    WEST(0, -1),
    NORTHWEST(1, -1);

    private int verticalShift;
    private int horizontalShift;
}
