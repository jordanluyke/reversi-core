package com.jordanluyke.reversi;

import com.google.inject.Singleton;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */

@Singleton
public class Config {

    public int port = 8080;
    public boolean sslEnabled = false;
    public SslContext sslContext = null;
    public String dbUrl;
    public String dbUser;
    public String dbPassword;

    public Config() {
        if(sslEnabled)
            this.sslContext = getSslCtx();
        load();
    }

    private void load() {
        try {
            Properties p = new Properties();
            p.load(new FileInputStream("src/main/resources/config.properties"));

            dbUrl = p.getProperty("db.url");
            dbUser = p.getProperty("db.user");
            dbPassword = p.getProperty("db.password");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SslContext getSslCtx() {
        SslContext sslCtx;
        try {
            SelfSignedCertificate ssc;
            try {
                ssc = new SelfSignedCertificate();
            } catch(CertificateException e) {
                throw new RuntimeException(e.getMessage());
            }
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch(SSLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return sslCtx;
    }
}
