package com.jordanluyke.reversi;

import com.google.inject.Guice;
import com.jordanluyke.reversi.util.ErrorHandlingSubscriber;
import com.jordanluyke.reversi.web.http.api.ApiManager;
import com.jordanluyke.reversi.web.http.api.ApiModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Initializing");

        Guice.createInjector(new ApiModule())
                .getInstance(ApiManager.class)
                .start()
                .subscribe(new ErrorHandlingSubscriber<>());
    }
}
