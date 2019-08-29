package com.jordanluyke.reversi.web.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.events.SocketEvent;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class ApiManagerImpl implements ApiManager {
    private static final Logger logger = LogManager.getLogger(ApiManager.class);

    private final ApiV1 apiV1 = new ApiV1();

    private Config config;

    @Override
    public Single<HttpServerResponse> handleRequest(HttpServerRequest request) {
        return Single.defer(() -> {
            logger.info("HttpRequest: {} {} {}", request.getCtx().channel().remoteAddress(), request.getMethod(), request.getPath());
            return Single.just(request.getMethod());
        })
                .flatMap(method -> {
                    if(!Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE).contains(method))
                        return Single.error(new WebException(HttpResponseStatus.METHOD_NOT_ALLOWED));
                    return Observable.fromIterable(apiV1.getHttpRoutes())
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
                })
                .doOnSuccess(res -> logger.info("HttpResponse: {} {}", request.getCtx().channel().remoteAddress(), res.getBody()))
                .onErrorResumeNext(err -> {
                    WebException e = (err instanceof WebException) ? (WebException) err : new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    logger.error("HttpResponse: {} {}", request.getCtx().channel().remoteAddress(), e.toHttpServerResponse().getBody());
                    if(!(err instanceof WebException))
                        err.printStackTrace();
                    return Single.just(e.toHttpServerResponse());
                });
    }

    @Override
    public Maybe<WebSocketServerResponse> handleRequest(WebSocketServerRequest request) {
        return Observable.defer(() -> {
            NodeUtil.get("event", request.getBody()).ifPresent(event -> {
                if(!event.equals(SocketEvent.KeepAlive.toString()))
                    logger.info("WebSocketRequest: {} {}", request.getConnection().getCtx().channel().remoteAddress(), request.getBody());
            });
            return Observable.fromIterable(apiV1.getWebSocketEvents());
        })
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
                .doOnSuccess(Void -> {
                    NodeUtil.get("receiptId", request.getBody()).ifPresent(receiptId -> {
                        WebSocketServerResponse receiptRes = WebSocketServerResponse.builder()
                                .event(SocketEvent.Receipt)
                                .body(NodeUtil.mapper.createObjectNode().put("id", receiptId))
                                .build();
                        request.getConnection().send(receiptRes);
                    });
                })
                .flatMapMaybe(instance -> ((WebSocketEventHandler) instance).handle(Single.just(request)))
                .doOnSuccess(res -> {
                    if(res.getEvent() != SocketEvent.KeepAlive)
                        logger.info("WebSocketResponse: {} {}", request.getConnection().getCtx().channel().remoteAddress(), res.toNode());
                })
                .onErrorResumeNext(err -> {
                    WebException e = (err instanceof WebException) ? (WebException) err : new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    logger.error("WebSocketResponse: {} {}", request.getConnection().getCtx().channel().remoteAddress(), e.toWebSocketServerResponse().toNode());
                    if(!(err instanceof WebException))
                        err.printStackTrace();
                    return Maybe.just(e.toWebSocketServerResponse());
                });
    }
}
