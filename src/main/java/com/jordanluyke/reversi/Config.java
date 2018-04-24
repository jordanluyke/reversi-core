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
    public String jdbcUrl;
    public String jdbcUser;
    public String jdbcPassword;

    public Config() {
        if(sslEnabled)
            this.sslContext = getSslCtx();
        loadConfig();
    }

    private void loadConfig() {
        try {
            Properties p = new Properties();
            p.load(new FileInputStream("src/main/resources/config.properties"));

            jdbcUrl = p.getProperty("jdbc.url");
            jdbcUser = p.getProperty("jdbc.user");
            jdbcPassword = p.getProperty("jdbc.password");
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
