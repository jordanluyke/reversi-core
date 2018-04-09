package com.jordanluyke.reversi.web.http.api.model;

import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ApiRoute {

    private String path;
    private HttpMethod method;
    private Class<? extends RouteHandler> handler;
    private int version;

    public ApiRoute(String path, HttpMethod method, Class<? extends RouteHandler> handler) {
        this.path = path;
        this.method = method;
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public Class<? extends RouteHandler> getHandler() {
        return handler;
    }

    public void setHandler(Class<? extends RouteHandler> handler) {
        this.handler = handler;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
