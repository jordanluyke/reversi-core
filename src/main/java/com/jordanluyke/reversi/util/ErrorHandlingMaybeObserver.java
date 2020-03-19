package com.jordanluyke.reversi.util;

import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.logging.log4j.LogManager;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ErrorHandlingMaybeObserver<T extends Object> implements MaybeObserver<T> {
    private Class<?> loggerClass;

    public ErrorHandlingMaybeObserver() {
        loggerClass = getClass();
    }

    @Override
    public void onSuccess(T t) {
    }

    @Override
    public void onError(Throwable e) {
        LogManager.getLogger(loggerClass).error("Error: {}", e.getMessage());
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onSubscribe(Disposable disposable) {
    }
}