package com.jordanluyke.reversi.db;

import io.reactivex.rxjava3.core.Completable;
import org.jooq.DSLContext;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface DbManager {

    Completable start();

    DSLContext getDsl();
}
