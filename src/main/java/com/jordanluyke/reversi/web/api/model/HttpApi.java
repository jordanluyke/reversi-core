package com.jordanluyke.reversi.web.api.model;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface HttpApi {

    int getVersion();

    List<HttpRoute> getRoutes();
}
