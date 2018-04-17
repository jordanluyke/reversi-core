package com.jordanluyke.reversi.db;

import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface DbManager {

    Observable<Void> start();
}
