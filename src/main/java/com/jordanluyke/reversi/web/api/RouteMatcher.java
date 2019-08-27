package com.jordanluyke.reversi.web.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.model.WebSocketRoute;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class RouteMatcher {
    private static final Logger logger = LogManager.getLogger(RouteMatcher.class);

    @Inject private Config config;

    private ApiV1 apiV1 = new ApiV1(1);
    private List<HttpRoute> routes = apiV1.getHttpRoutes();
    private List<WebSocketRoute> events = apiV1.getWebSocketEvents();

    public Single<HttpServerResponse> handle(HttpServerRequest request) {
        return Single.just(request.getMethod())
                .flatMap(method -> {
                    if(!Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE).contains(method))
                        return Single.error(new WebException(HttpResponseStatus.METHOD_NOT_ALLOWED));
                    return Observable.fromIterable(routes)
                            .filter(route -> {
                                if(request.getMethod() != route.getMethod())
                                    return false;

                                String[] splitRequestPath = request.getPath().split("/");
                                String[] splitRouteHandlerPath = route.getPath().split("/");

                                if(splitRequestPath.length != splitRouteHandlerPath.length)
                                    return false;

                                IntStream.range(0, splitRouteHandlerPath.length)
                                        .filter(i -> splitRouteHandlerPath[i].startsWith(":"))
                                        .forEach(i -> {
                                            splitRequestPath[i] = "*";
                                            splitRouteHandlerPath[i] = "*";
                                        });

                                String joinedRequestPath = String.join("/", splitRequestPath);
                                String joinedRouteHandlerPath = String.join("/", splitRouteHandlerPath);

                                return joinedRequestPath.equals(joinedRouteHandlerPath);
                            })
                            .singleOrError()
                            .onErrorResumeNext(err -> Single.error(new WebException(HttpResponseStatus.NOT_FOUND)));
                })
                .flatMap(route -> {
                    List<String> splitRouteHandlerPath = Arrays.asList(route.getPath().split("/"));
                    List<String> splitRequestPath = Arrays.asList(request.getPath().split("/"));

                    List<Integer> paramIndexes = new ArrayList<>();
                    for(int i = 0; i < splitRouteHandlerPath.size(); i++)
                        if(splitRouteHandlerPath.get(i).startsWith(":"))
                            paramIndexes.add(i);

                    Map<String, String> params = request.getQueryParams();
                    paramIndexes.forEach(i -> {
                        String name = splitRouteHandlerPath.get(i).substring(1);
                        String value = splitRequestPath.get(i);
                        params.put(name, value);
                    });
                    request.setQueryParams(params);

                    return Single.just(route.getHandler());
                })
                .map(clazz -> config.getInjector().getInstance(clazz))
                .flatMap(instance -> instance.handle(Single.just(request)))
                .flatMap(object -> {
                    if(object instanceof HttpServerResponse)
                        return Single.just((HttpServerResponse) object);

                    HttpServerResponse res = new HttpServerResponse();
                    res.setStatus(HttpResponseStatus.OK);

                    if(object instanceof ObjectNode) {
                        res.setBody((ObjectNode) object);
                    } else {
                        try {
                            res.setBody(NodeUtil.mapper.valueToTree(object));
                        } catch(IllegalArgumentException e) {
                            logger.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
                            return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                        }
                    }

                    return Single.just(res);
                });
    }

    public Maybe<WebSocketServerResponse> handle(WebSocketServerRequest request) {
        return Observable.fromIterable(events)
                .filter(event -> {
                    Optional<String> e = NodeUtil.get("event", request.getBody());
                    return e.isPresent() && event.getType().getSimpleName().equals(e.get());
                })
                .singleOrError()
                .onErrorResumeNext(err -> Single.error(new WebException(HttpResponseStatus.NOT_FOUND)))
                .flatMap(event -> {
                    try {
                        return Single.just(config.getInjector().getInstance(Class.forName(event.getType().getName())));
                    } catch(ClassNotFoundException e) {
                        logger.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
                        return Single.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                    }
                })
                .flatMapMaybe(instance -> ((WebSocketEventHandler) instance).handle(Single.just(request)));
    }
}
