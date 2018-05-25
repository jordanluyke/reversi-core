package com.jordanluyke.reversi.web.api.model;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@AllArgsConstructor
public class HttpRoute {

    private String path;
    private HttpMethod method;
    private Class<? extends HttpRouteHandler> handler;
}
