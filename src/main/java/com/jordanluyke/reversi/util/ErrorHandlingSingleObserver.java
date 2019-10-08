package com.jordanluyke.reversi.util;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.logging.log4j.LogManager;

public class ErrorHandlingSingleObserver<T extends Object> implements SingleObserver<T> {
    private Class<?> loggerClass;

    public ErrorHandlingSingleObserver() {
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
    public void onSubscribe(Disposable disposable) {
    }
}