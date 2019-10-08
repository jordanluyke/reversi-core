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
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private Injector injector;
    private String pusherAppId;
    private String pusherKey;
    private String pusherSecret;
    private String pusherCluster;

    public Config() {
        load();
        logger.info("Config loaded");
    }

    private void load() {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("config.properties"));
        } catch(IOException e1) {
            try {
                p.load(new FileInputStream("src/main/resources/config.properties"));
            } catch(IOException e2) {
                throw new RuntimeException("Unable to load config.properties file");
            }
        }

        jdbcUrl = p.getProperty("jdbc.url");
        jdbcUser = p.getProperty("jdbc.user");
        jdbcPassword = p.getProperty("jdbc.password");
        pusherAppId = p.getProperty("pusher.appId");
        pusherKey = p.getProperty("pusher.key");
        pusherSecret = p.getProperty("pusher.secret");
        pusherCluster = p.getProperty("pusher.cluster");

        try {
            FileInputStream cert = new FileInputStream("cert.pem");
            FileInputStream key = new FileInputStream("key-pkcs8.pem");
            sslContext = Optional.of(SslContextBuilder.forServer(cert, key).build());
            port = 8443;
        } catch(SSLException e) {
            logger.error(e.getMessage());
        } catch(FileNotFoundException e) {
        }
    }
}
