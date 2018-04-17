package com.jordanluyke.reversi.db;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class DbManagerImpl implements DbManager {
    private static final Logger logger = LogManager.getLogger(DbManager.class);

    private Config config;

    @Inject
    public DbManagerImpl(Config config) {
        this.config = config;
    }

    @Override
    public Observable<Void> start() {
        try {
            Properties properties = new Properties();
            properties.setProperty("user", config.dbUser);
            properties.setProperty("password", config.dbPassword);
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", "true");
            properties.setProperty("useUnicode", "true");
            properties.setProperty("useJDBCCompliantTimezoneShift", "true");
            properties.setProperty("serverTimezone", "UTC");

            Connection connection = DriverManager.getConnection(config.dbUrl, properties);
            logger.info("Connected to mysql");
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Observable.empty();
    }
}
