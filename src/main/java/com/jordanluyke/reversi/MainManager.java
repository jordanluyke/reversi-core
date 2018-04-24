package com.jordanluyke.reversi;

import com.google.inject.Injector;
import rx.Observable;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface MainManager {

    Observable<Void> start(Injector injector);

    Injector getInjector();
}
