package com.jordanluyke.reversi.web.api;

import com.google.inject.Inject;
import com.jordanluyke.reversi.Config;
import com.jordanluyke.reversi.util.NodeUtil;
import com.jordanluyke.reversi.web.api.model.SocketChannel;
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
        setup();
    }

    @Override
    public void send(SocketChannel channel, String event) {
        pusher.trigger(channel.name(), event, NodeUtil.mapper.createObjectNode());
    }

    private void setup() {
        pusher = new Pusher(config.getPusherAppId(), config.getPusherKey(), config.getPusherSecret());
        pusher.setCluster(config.getPusherCluster());
        pusher.setEncrypted(true);
    }
}
