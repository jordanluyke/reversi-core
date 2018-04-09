package com.jordanluyke.reversi.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Subscriber;

public class ErrorHandlingSubscriber<T extends Object> extends Subscriber<T> {
    private static final Logger logger = LogManager.getLogger(ErrorHandlingSubscriber.class);
    private Class<?> loggerClass;

    public ErrorHandlingSubscriber() {
        loggerClass = getClass();
    }

    @Override
    public void onCompleted() {}

    @Override
    public void onError(Throwable e) {
        LogManager.getLogger(loggerClass).error("Error", e);
    }

    @Override
    public void onNext(Object voidObservable) {
    }
}