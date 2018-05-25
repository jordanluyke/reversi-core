package com.jordanluyke.reversi.db;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import rx.Observable;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class DbManagerImpl implements DbManager {
    private static final Logger logger = LogManager.getLogger(DbManager.class);

    private Config config;
    private DSLContext dsl;

    @Inject
    public DbManagerImpl(Config config) {
        this.config = config;
    }

    @Override
    public Observable<Void> start() {
        try {
            Connection connection = DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPassword());
            System.getProperties().setProperty("org.jooq.no-logo", "true");
            Settings settings = new Settings()
                    .withExecuteLogging(false);
            this.dsl = DSL.using(connection, SQLDialect.MYSQL, settings);
            logger.info("Connected to mysql");
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Observable.empty();
    }

    public DSLContext getDsl() {
        return dsl;
    }
}
