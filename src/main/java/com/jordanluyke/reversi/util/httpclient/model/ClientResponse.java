package com.jordanluyke.reversi.util.httpclient.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ClientResponse {

    private int statusCode;
    private byte[] rawBody;
    private Map<String, String> headers = new HashMap<>();

    public ClientResponse() {
    }

    public void validate() {
        if(rawBody == null)
            throw new RuntimeException("body is null");
        if(statusCode == -1)
            throw new RuntimeException("statusCode is null");
    }

    public String getBody() {
        return new String(rawBody, StandardCharsets.UTF_8);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
    }
}
