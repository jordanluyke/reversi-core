package com.jordanluyke.reversi.util;

import io.reactivex.rxjava3.functions.BiConsumer;
import org.apache.logging.log4j.LogManager;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ErrorHandlingBiConsumer<T extends Object> implements BiConsumer<T, Throwable> {
    private Class<?> loggerClass;

    public ErrorHandlingBiConsumer() {
        loggerClass = getClass();
    }

    @Override
    public void accept(T t, Throwable throwable) throws Throwable {
        if(t == null) {
        } else if(throwable != null) {
            LogManager.getLogger(loggerClass).error("Error: {}", throwable.getMessage());
        }
    }
}
