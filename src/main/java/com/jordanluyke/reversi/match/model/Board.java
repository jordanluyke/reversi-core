package com.jordanluyke.reversi.match.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rx.Observable;

import java.util.Arrays;
import java.util.List;

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

    public Observable<Integer> getAmount(Side side) {
        return Observable.from(squares)
                .filter(square -> square == side)
                .toList()
                .map(List::size);
    }

    public Observable<Void> placePiece(Side side, Position position) {
        if(squares[position.getIndex()] != null)
            return Observable.error(new IllegalMoveException());
        return Observable.from(Direction.class.getEnumConstants())
                .filter(direction -> position.isPositionValid(direction) && squares[position.getPosition(direction).getIndex()] == side.getOpposite())
                .flatMap(direction -> getConnectingPositions(side, Arrays.asList(position), direction))
                .defaultIfEmpty(null)
                .flatMap(pos -> {
                    if(pos == null)
                        return Observable.error(new IllegalMoveException());
                    return Observable.just(pos);
                })
                .doOnNext(pos -> squares[pos.getIndex()] = side)
                .toList()
                .doOnNext(Void -> transcript += position.getCoordinates())
                .ignoreElements()
                .cast(Void.class);
    }

    private Observable<Position> getConnectingPositions(Side side, List<Position> positions, Direction direction) {
        Position lastPosition = positions.get(positions.size() - 1);

        if(!lastPosition.getPosition(direction).isPositionValid(direction))
            return Observable.empty();

        Position currentPosition = lastPosition.getPosition(direction);
        Side square = squares[currentPosition.getIndex()];

        if(square == null)
            return Observable.empty();

        if(square == side)
            return Observable.from(positions);

        positions.add(currentPosition);

        return getConnectingPositions(side, positions, direction);
    }
}
