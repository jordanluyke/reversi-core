package com.jordanluyke.reversi.db;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
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
            logger.info("Starting Flyway");
            Flyway.configure().dataSource(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPassword()).load().migrate();
            logger.info("Migration successful");
            Connection connection = DriverManager.getConnection(config.getJdbcUrl(), config.getJdbcUser(), config.getJdbcPassword());
            this.dsl = DSL.using(connection, SQLDialect.MYSQL);
            logger.info("Connected to MySQL");
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Observable.empty();
    }

    public DSLContext getDsl() {
        return dsl;
    }
}
