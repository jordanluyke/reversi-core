package com.jordanluyke.reversi.match.model;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class Position {
    private static final Logger logger = LogManager.getLogger(Position.class);

    private static String[] horizontalPositions = "ABCDEFGH".split("");
    private static String[] verticalPositions = "12345678".split("");

    @Getter private int index;

    private Position(int index) {
        this.index = index;
    }

    public static Position fromIndex(int index) {
        if(index < 0 || index >= 64)
            throw new RuntimeException("Index out of bounds");
        return new Position(index);
    }

    public static Position fromCoordinates(String coords) {
        if(coords.length() != 2)
            throw new RuntimeException("Invalid coordinates");
        coords = coords.toUpperCase();
        List<String> hPositions = Arrays.asList(horizontalPositions);
        List<String> vPositions = Arrays.asList(verticalPositions);
        String hChar = String.valueOf(coords.charAt(0));
        String vChar = String.valueOf(coords.charAt(1));
        if(!hPositions.contains(hChar) || !vPositions.contains(vChar))
            throw new RuntimeException("Invalid coordinates");
        return new Position((vPositions.indexOf(vChar) * 8) + hPositions.indexOf(hChar));
    }

    public String getCoordinates() {
        String h = horizontalPositions[Math.floorDiv(index, 8)];
        String v = verticalPositions[index % 8];
        return h + v;
    }

    public Position getNewPosition(Direction direction) {
        int i = getNewIndexPosition(direction);
        if(!isIndexInBounds(i))
            throw new RuntimeException("Index out of bounds");
        return new Position(i);
    }

    public boolean isWithinBounds(Direction direction) {
        return isIndexInBounds(getNewIndexPosition(direction));
    }

    private boolean isIndexInBounds(int index) {
        return index >= 0 && index < 64;
    }

    private int getNewIndexPosition(Direction direction) {
        if((index % 8 == 0 && direction.getHorizontalShift() == -1) || (index % 8 == 7 && direction.getHorizontalShift() == 1))
            return -1;
        return index + (8 * direction.getVerticalShift()) + direction.getHorizontalShift();
    }
}
