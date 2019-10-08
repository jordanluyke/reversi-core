package com.jordanluyke.reversi.web.api.model;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public interface Api {

    int getVersion();

    List<HttpRoute> getHttpRoutes();
}
