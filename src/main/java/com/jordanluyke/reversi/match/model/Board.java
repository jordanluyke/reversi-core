package com.jordanluyke.reversi.match.model;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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

    public Single<Integer> getAmount(Side side) {
        return Observable.fromArray(squares)
                .filter(square -> square == side)
                .toList()
                .map(List::size);
    }

    public Single<Board> placePiece(Side side, Position position) {
        if(squares[position.getIndex()] != null)
            return Single.error(new IllegalMoveException());
        return Observable.fromArray(Direction.class.getEnumConstants())
                .filter(direction -> position.isWithinBounds(direction) && squares[position.getNewPosition(direction).getIndex()] == side.getOpposite())
                .flatMap(direction -> getConnectingPositions(side, position, direction))
                .switchIfEmpty(Observable.error(new IllegalMoveException()))
                .doOnNext(pos -> squares[pos.getIndex()] = side)
                .toList()
                .doOnSuccess(Void -> transcript += position.getCoordinates())
                .map(Void -> this);
    }

    public Single<Boolean> canPlacePiece(Side side) {
        return Observable.range(0, squares.length)
                .filter(index -> squares[index] == null)
                .flatMap(index -> Observable.fromArray(Direction.class.getEnumConstants())
                        .filter(direction -> Position.fromIndex(index).isWithinBounds(direction) && squares[Position.fromIndex(index).getNewPosition(direction).getIndex()] == side.getOpposite())
                        .flatMap(direction -> getConnectingPositions(side, Position.fromIndex(index), direction))
                )
                .toList()
                .map(Void -> true)
                .onErrorResumeNext(e -> Single.just(false));
    }

    private Observable<Position> getConnectingPositions(Side side, Position startPosition, Direction direction) {
        return getConnectingPositions(side, Arrays.asList(startPosition), direction);
    }

    private Observable<Position> getConnectingPositions(Side side, List<Position> positions, Direction direction) {
        positions = new ArrayList<>(positions);
        Position lastPosition = positions.get(positions.size() - 1);

        if(!lastPosition.isWithinBounds(direction))
            return Observable.empty();

        Position currentPosition = lastPosition.getNewPosition(direction);
        Side square = squares[currentPosition.getIndex()];

        if(square == null)
            return Observable.empty();

        if(square == side)
            return Observable.fromIterable(positions);

        positions.add(currentPosition);

        return getConnectingPositions(side, positions, direction);
    }
}
