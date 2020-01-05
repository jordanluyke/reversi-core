package com.jordanluyke.reversi;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class TestEnv {
    private static final Logger logger = LogManager.getLogger(Main.class);

    private Injector injector;

    public TestEnv() {
        injector = Guice.createInjector(new MainModule());
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }
}
