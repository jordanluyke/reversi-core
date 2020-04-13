package com.jordanluyke.reversi;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */

@Getter
@Setter
@Singleton
public class Config {
    private static final Logger logger = LogManager.getLogger(Config.class);

    private int port = 8080;
    private Optional<SslContext> sslContext = Optional.empty();
    // config.properties
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private Injector injector;
    private String pusherAppId;
    private String pusherKey;
    private String pusherSecret;
    private String pusherCluster;
    // build.properties
    private String branch;
    private long builtAt;
    private String commit;

    public Config() {
        load();
        logger.info("Config loaded");
    }

    private void load() {
        Properties cp = new Properties();
        try {
            cp.load(new FileInputStream("config.properties"));
        } catch(IOException e1) {
            try {
                cp.load(new FileInputStream("src/main/resources/config.properties"));
            } catch(IOException e2) {
                throw new RuntimeException("Unable to load config.properties file");
            }
        }

        jdbcUrl = cp.getProperty("jdbc.url");
        jdbcUser = cp.getProperty("jdbc.user");
        jdbcPassword = cp.getProperty("jdbc.password");
        pusherAppId = cp.getProperty("pusher.appId");
        pusherKey = cp.getProperty("pusher.key");
        pusherSecret = cp.getProperty("pusher.secret");
        pusherCluster = cp.getProperty("pusher.cluster");

        try {
            FileInputStream cert = new FileInputStream("cert.pem");
            FileInputStream key = new FileInputStream("key-pkcs8.pem");
            sslContext = Optional.of(SslContextBuilder.forServer(cert, key).build());
            port = 8443;
        } catch(SSLException e) {
            logger.error(e.getMessage());
        } catch(FileNotFoundException e) {
        }

        Properties bp = new Properties();
        try {
            bp.load(new FileInputStream("build.properties"));
        } catch(IOException e1) {
            try {
                bp.load(new FileInputStream("target/build.properties"));
            } catch(IOException e2) {
                throw new RuntimeException("Unable to load build.properties file");
            }
        }

        branch = bp.getProperty("branch");
        builtAt = Long.parseLong(bp.getProperty("builtAt"));
        commit = bp.getProperty("commit");
    }
}
