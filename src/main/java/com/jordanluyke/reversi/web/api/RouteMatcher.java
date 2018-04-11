package com.jordanluyke.reversi.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jordanluyke.reversi.util.RandomUtil;
import com.jordanluyke.reversi.web.api.model.HttpRoute;
import com.jordanluyke.reversi.web.api.model.HttpException;
import com.jordanluyke.reversi.web.model.ServerRequest;
import com.jordanluyke.reversi.web.model.ServerResponse;
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

    private List<HttpRoute> routes = new ArrayList<>(new HttpApiV1().getRoutes());

    public Observable<ServerResponse> handle(ServerRequest request) {
        Injector injector = Guice.createInjector(new ApiModule());

        logger.info("{} {}", request.getMethod(), request.getPath());

        return Observable.just(request.getMethod())
                .flatMap(method -> {
                    if(!Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE).contains(method))
                        return Observable.error(new HttpException("Invalid HTTP method", HttpResponseStatus.METHOD_NOT_ALLOWED, "MethodNotAllowedException"));
                    return Observable.from(routes);
                })
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
                .defaultIfEmpty(null)
                .flatMap(route -> {
                    if(route == null)
                        return Observable.error(new HttpException("Invalid path", HttpResponseStatus.NOT_FOUND, "NotFoundException"));

                    List<String> splitRouteHandlerPath = Arrays.asList(route.getPath().split("/"));
                    List<String> splitRequestPath = Arrays.asList(request.getPath().split("/"));

                    List<Integer> paramIndexes = new ArrayList<>();
                    for(int i = 0; i < splitRouteHandlerPath.size(); i++) {
                        if(splitRouteHandlerPath.get(i).startsWith(":")) {
                            paramIndexes.add(i);
                        }
                    }

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
                .map(node -> {
                    ServerResponse response = new ServerResponse();
                    response.setStatus(HttpResponseStatus.OK);
                    response.setBody(node);
                    return response;
                })
                .onErrorResumeNext(err -> {
                    ServerResponse response = new ServerResponse();
                    ObjectNode body = new ObjectMapper().createObjectNode();
                    body.put("exceptionId", RandomUtil.generateRandom(6));

                    if(err instanceof HttpException) {
                        logger.error("{}: {}", ((HttpException) err).getExceptionType(), err.getMessage());
                        response.setStatus(((HttpException) err).getStatus());
                        body.put("message", err.getMessage());
                        body.put("exceptionType", ((HttpException) err).getExceptionType());
                    } else {
                        response.setStatus(HttpResponseStatus.BAD_REQUEST);
                        body.put("message", "Something went wrong");
                        body.put("exceptionType", "BadRequestException");
                    }

                    response.setBody(body);
                    return Observable.just(response);
                });
    }
}
