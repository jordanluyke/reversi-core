package com.jordanluyke.reversi.db;

import org.jooq.DSLContext;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface DbManager {

    Observable<Void> start();

    public DSLContext getDslContext();
}
