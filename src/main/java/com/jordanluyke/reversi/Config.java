package com.jordanluyke.reversi;

import com.google.inject.Singleton;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */

@Singleton
public class Config {

    public int httpPort = 8080;
    public int wsPort = 8000;
    public boolean sslEnabled = false;
    public SslContext sslContext = null;

    public Config() {
        if(sslEnabled)
            this.sslContext = getSslCtx();
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
