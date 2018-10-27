package com.jordanluyke.reversi;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */

@Getter
@Setter
@Singleton
public class Config {

    private int port = 8080;
    private boolean sslEnabled = false;
    private SslContext sslContext = null;
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private Injector injector;

    public Config() {
        if(sslEnabled)
            this.sslContext = getSslCtx();
        loadConfig();
    }

    private void loadConfig() {
        try {
            Properties p = new Properties();
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream("src/main/resources/config.properties");
            } catch(FileNotFoundException e) {
                fileInputStream = new FileInputStream("config.properties");
            }
            p.load(fileInputStream);

            jdbcUrl = p.getProperty("jdbc.url");
            jdbcUser = p.getProperty("jdbc.user");
            jdbcPassword = p.getProperty("jdbc.password");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SslContext getSslCtx() {
        try {
            SelfSignedCertificate ssc;
            try {
                ssc = new SelfSignedCertificate();
            } catch(CertificateException e) {
                throw new RuntimeException(e.getMessage());
            }
            return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch(SSLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
