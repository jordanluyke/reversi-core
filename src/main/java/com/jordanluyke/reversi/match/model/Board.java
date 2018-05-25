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
public class Board {

    private Side[] squares = new Side[64];

    public String getLocation(int index) {
        if(index < 0 || index >= 64)
            throw new Error("Index out of bounds");
        String[] horizontalLocations = "ABCDEFGH".split("");
        String[] verticalLocations = "12345678".split("");
        String h = horizontalLocations[Math.floorDiv(index, 8)];
        String v = verticalLocations[index % 8];
        return h + v;
    }

    public int getIndexRelativeTo(int index, Direction direction) {
        int i = index + (8 * direction.getVerticalShift()) + direction.getHorizontalShift();
        if(i < 0 || i >= 64)
            throw new Error("Index out of bounds");
        return i;
    }
}
