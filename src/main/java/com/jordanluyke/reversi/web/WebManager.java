package com.jordanluyke.reversi.web;

import io.reactivex.Completable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface WebManager {

    Completable start();
}
