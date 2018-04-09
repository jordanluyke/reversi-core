package com.jordanluyke.reversi.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Hasher {
    private static final MessageDigest sha256;
    private static final MessageDigest md5;
    private static final Mac sha256hmac;
    private static final Mac sha1hmac;
    private static final Mac md5hmac;

    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
            md5 = MessageDigest.getInstance("MD5");
            sha256hmac = Mac.getInstance("HmacSHA256");
            sha1hmac = Mac.getInstance("HmacSHA1");
            md5hmac = Mac.getInstance("HmacMD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize Hasher", e);
        }
    }

    private Hasher() {
    }

    static public String sha256base64(String data, String salt) {
        return sha256base64(data + salt);
    }

    static public String sha256base64(String data) {
        return new String(Base64.getEncoder().encode(sha256.digest((data).getBytes())));
    }

    static public String sha256hex(String data) {
        return bytesToHex(sha256.digest((data).getBytes()));
    }

    static public String md5base64(String data, String nonce) {
        return md5base64(data + nonce);
    }

    static public String md5base64(String data) {
        return new String(Base64.getEncoder().encode(md5.digest((data).getBytes())));
    }

    static public String md5hex(String data) {
        return bytesToHex(md5.digest((data).getBytes()));
    }

    static public byte[] sha256bytes(String data){
        return sha256.digest(data.getBytes());
    }

    static public String hmacSha256(String key, String data) {
        return encodeHmac(sha256hmac, key, data);
    }

    static public String hmacSha1(String key, String data) {
        return encodeHmac(sha1hmac, key, data);
    }

    static public String hmacMd5(String key, String data) {
        return encodeHmac(md5hmac, key, data);
    }

    static private String encodeHmac(Mac hmac, String key, String data) {
        try {
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), hmac.getAlgorithm());
            hmac.init(secret_key);
            return bytesToHex(hmac.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            return "";
        }
    }

    static public String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for(byte b : bytes)
            builder.append(String.format("%02x", b));
        return builder.toString();
    }
}

