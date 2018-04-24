package com.jordanluyke.reversi.db;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
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
    private DSLContext dslContext;

    @Inject
    public DbManagerImpl(Config config) {
        this.config = config;
    }

    @Override
    public Observable<Void> start() {
        try {
            Properties properties = new Properties();
            properties.setProperty("user", config.jdbcUser);
            properties.setProperty("password", config.jdbcPassword);

            Connection connection = DriverManager.getConnection(config.jdbcUrl, config.jdbcUser, config.jdbcPassword);
            DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);
            this.dslContext = dslContext;
            logger.info("Connected to mysql");
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Observable.empty();
    }

    public DSLContext getDslContext() {
        return dslContext;
    }
}
