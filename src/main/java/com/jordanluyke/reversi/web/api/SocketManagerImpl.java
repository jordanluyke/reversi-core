package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.SocketEvent;
import com.pusher.rest.Pusher;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class SocketManagerImpl implements SocketManager {

    private Config config;
    private Pusher pusher;

    @Inject
    public SocketManagerImpl(Config config) {
        this.config = config;
        pusher = new Pusher(config.getPusherAppId(), config.getPusherKey(), config.getPusherSecret());
        pusher.setCluster(config.getPusherCluster());
    }

    @Override
    public void send(SocketEvent event, String channel) {
        pusher.trigger(channel, event.name(), NodeUtil.mapper.createObjectNode());
    }
}
