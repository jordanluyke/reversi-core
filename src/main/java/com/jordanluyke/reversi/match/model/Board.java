package com.jordanluyke.reversi.match.model;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Board {
    private static final Logger logger = LogManager.getLogger(Board.class);

    private Side[] squares;
    private String transcript;

    public static Board create() {
        Side[] squares = new Side[64];
        squares[Position.fromCoordinates("D4").getIndex()] = Side.LIGHT;
        squares[Position.fromCoordinates("E4").getIndex()] = Side.DARK;
        squares[Position.fromCoordinates("D5").getIndex()] = Side.DARK;
        squares[Position.fromCoordinates("E5").getIndex()] = Side.LIGHT;
        return new Board(squares, "");
    }

    public int getAmount(Side side) {
        return (int) Arrays.stream(squares)
                .filter(square -> square == side)
                .count();
    }

    public void placePiece(Side side, Position position) throws IllegalMoveException {
        if(squares[position.getIndex()] != null)
            throw new IllegalMoveException();
        List<Position> connectingPositions = Arrays.stream(Direction.class.getEnumConstants())
                .filter(direction -> position.isWithinBounds(direction) && squares[position.getNewPosition(direction).getIndex()] == side.getOpposite())
                .map(direction -> getConnectingPositions(side, position, direction))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if(connectingPositions.size() == 0)
            throw new IllegalMoveException();
        connectingPositions.forEach(pos -> squares[pos.getIndex()] = side);
        transcript += position.getCoordinates();
    }

    public boolean canPlacePiece(Side side) {
        return getValidPositions(side).size() > 0;

    }

    public boolean isComplete() {
        return Arrays.stream(squares).noneMatch(Objects::isNull);
    }

    private List<Position> getValidPositions(Side side) {
        return IntStream.range(0, squares.length)
                .filter(index -> squares[index] == null)
                .mapToObj(index -> Arrays.stream(Direction.class.getEnumConstants())
                            .filter(direction -> Position.fromIndex(index).isWithinBounds(direction) && squares[Position.fromIndex(index).getNewPosition(direction).getIndex()] == side.getOpposite())
                            .map(direction -> getConnectingPositions(side, Position.fromIndex(index), direction))
                            .filter(list -> list.size() > 0)
                            .map(list -> list.get(0))
                            .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Position> getConnectingPositions(Side side, Position startPosition, Direction direction) {
        return getConnectingPositions(side, Collections.singletonList(startPosition), direction);
    }

    private List<Position> getConnectingPositions(Side side, List<Position> positions, Direction direction) {
        positions = new ArrayList<>(positions);
        Position lastPosition = positions.get(positions.size() - 1);

        if(!lastPosition.isWithinBounds(direction))
            return Collections.emptyList();

        Position currentPosition = lastPosition.getNewPosition(direction);
        Side square = squares[currentPosition.getIndex()];

        if(square == null)
            return Collections.emptyList();

        if(square == side)
            return positions;

        positions.add(currentPosition);

        return getConnectingPositions(side, positions, direction);
    }
}
