package com.jordanluyke.reversi.web.api.model;

import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class HttpRoute {

    private String path;
    private HttpMethod method;
    private Class<? extends HttpRouteHandler> handler;

    public HttpRoute(String path, HttpMethod method, Class<? extends HttpRouteHandler> handler) {
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

    public Class<? extends HttpRouteHandler> getHandler() {
        return handler;
    }
}
