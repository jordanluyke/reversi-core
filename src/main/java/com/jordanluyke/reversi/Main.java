package com.jordanluyke.reversi;

import com.google.inject.Guice;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.web.WebServer;
import com.jordanluyke.reversi.web.WebServerModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Initializing");

        Guice.createInjector(new WebServerModule())
                .getInstance(WebServer.class)
                .start()
                .subscribe(new ErrorHandlingSubscriber<>());
    }
}
