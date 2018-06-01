package com.jordanluyke.reversi.match.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class Position {

    private static String[] horizontalPositions = "ABCDEFGH".split("");
    private static String[] verticalPositions = "12345678".split("");

    @Getter private int index;

    private Position(int index) {
        this.index = index;
    }

    public static Position fromIndex(int index) {
        if(index < 0 || index >= 64)
            throw new Error("Index out of bounds");
        return new Position(index);
    }

    public static Position fromCoordinates(String coords) {
        if(coords.length() != 2)
            throw new Error("Invalid coordinates");
        coords = coords.toUpperCase();
        List<String> hPositions = Arrays.asList(horizontalPositions);
        List<String> vPositions = Arrays.asList(verticalPositions);
        String hChar = String.valueOf(coords.charAt(0));
        String vChar = String.valueOf(coords.charAt(1));
        if(!hPositions.contains(hChar) || !vPositions.contains(vChar))
            throw new Error("Invalid coordinates");
        return new Position((vPositions.indexOf(vChar) * 8) + hPositions.indexOf(hChar));
    }

    public String getCoordinates() {
        String h = horizontalPositions[Math.floorDiv(index, 8)];
        String v = verticalPositions[index % 8];
        return h + v;
    }

    public Position getPositionOfDirection(Direction direction) {
        int i = index + (8 * direction.getVerticalShift()) + direction.getHorizontalShift();
        if(i < 0 || i >= 64)
            throw new Error("Index out of bounds");
        return new Position(i);
    }
}
