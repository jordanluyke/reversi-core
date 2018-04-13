package com.jordanluyke.reversi.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.model.WebSocketEvent;
import com.jordanluyke.reversi.web.api.model.WebSocketEventHandler;
import com.jordanluyke.reversi.web.model.HttpServerResponse;
import com.jordanluyke.reversi.web.model.WebSocketServerRequest;
import com.jordanluyke.reversi.web.model.WebSocketServerResponse;
import com.jordanluyke.reversi.web.model.exceptions.*;
import com.jordanluyke.reversi.web.model.HttpServerRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class RouteMatcher {
    private static final Logger logger = LogManager.getLogger(RouteMatcher.class);

    private Injector injector = Guice.createInjector(new ApiModule());
    private ApiV1 apiV1 = new ApiV1();
    private List<HttpRoute> routes = apiV1.getHttpRoutes();
    private List<WebSocketEvent> events = apiV1.getWebSocketEvents();

    public Observable<HttpServerResponse> handle(HttpServerRequest request) {
        logger.info("{} {}", request.getMethod(), request.getPath());

        return Observable.just(request.getMethod())
                .flatMap(method -> {
                    if(!Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE).contains(method))
                        return Observable.error(new HttpException("Invalid HTTP method", HttpResponseStatus.METHOD_NOT_ALLOWED, "MethodNotAllowedException"));
                    return Observable.from(routes);
                })
                .filter(route -> request.getMethod() == route.getMethod())
                .filter(route -> {
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
                .take(1)
                .defaultIfEmpty(null)
                .flatMap(route -> {
                    if(route == null)
                        return Observable.error(new NotFoundException());

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

                    return Observable.just(route.getHandler());
                })
                .map(injector::getInstance)
                .flatMap(instance -> instance.handle(Observable.just(request)))
//                .map(object -> {
//                    if(object instanceof ObjectNode) {
//                        ObjectNode node = (ObjectNode) object;
//                        HttpServerResponse res = new HttpServerResponse();
//                        res.setStatus(HttpResponseStatus.OK);
//                        res.setBody(node);
//                        return res;
//                    } else if(object instanceof HttpServerResponse) {
//                        return (HttpServerResponse) object;
//                    } else
//                        throw new RuntimeException("Invalid handler object");
//                })
                .map(node -> {
                    HttpServerResponse res = new HttpServerResponse();
                    res.setStatus(HttpResponseStatus.OK);
                    res.setBody(node);
                    return res;
                })
                .onErrorResumeNext(err -> {
                    HttpException e = (err instanceof HttpException) ? (HttpException) err : new BadRequestException();
                    return Observable.just(exceptionToResponse(e));
                });
    }

    public Observable<WebSocketServerResponse> handle(WebSocketServerRequest request) {
        return Observable.from(events)
                .filter(event -> event.getType().getSimpleName().equals(request.getBody().get("event").textValue()))
                .take(1)
                .defaultIfEmpty(null)
                .flatMap(event -> {
                    if(event == null)
                        return Observable.error(new WsNotFoundException());
                    return Observable.just(injector.getInstance(event.getType()));
                })
                .flatMap(instance -> ((WebSocketEventHandler) instance).handle(Observable.just(request)))
                .map(node -> {
                    ObjectNode n = ((ObjectNode) node).deepCopy();
                    n.put("event", request.getBody().get("event").asText());
                    WebSocketServerResponse res = new WebSocketServerResponse();
                    res.setBody(n);
                    return res;
                })
                .onErrorResumeNext(err -> {
                    WsException e = (err instanceof WsException) ? (WsException) err : new WsBadRequestException();
                    return Observable.just(exceptionToResponse(e));
                });
    }


    private HttpServerResponse exceptionToResponse(HttpException e) {
        HttpServerResponse res = new HttpServerResponse();
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("exceptionId", RandomUtil.generateRandom(6));
        body.put("message", e.getMessage());
        body.put("exceptionType", e.getExceptionType());
        res.setBody(body);
        res.setStatus(e.getStatus());
        return res;
    }

    private WebSocketServerResponse exceptionToResponse(WsException e) {
        WebSocketServerResponse res = new WebSocketServerResponse();
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("exceptionId", RandomUtil.generateRandom(6));
        body.put("message", e.getMessage());
        body.put("exceptionType", e.getExceptionType());
        res.setBody(body);
        return res;
    }
}
