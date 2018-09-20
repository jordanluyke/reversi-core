package com.jordanluyke.reversi;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import io.netty.util.ResourceLeakDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        logger.info("Initializing");
        Injector injector = Guice.createInjector(new MainModule());

        injector.getInstance(MainManager.class)
                .start(injector)
                .subscribe(new ErrorHandlingSubscriber<>());
    }
}
