package com.jordanluyke.reversi.web.api.model;

import com.jordanluyke.reversi.web.model.HttpServerRequest;
import io.reactivex.Single;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface HttpRouteHandler {

    Single<?> handle(Single<HttpServerRequest> o);
}
