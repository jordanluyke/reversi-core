package com.jordanluyke.reversi.web;

import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface WebServer {

    Observable<Void> start();
}
