package com.lsx.rpc.service;

import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private Map<String, Object> serviceBeanMap;

    public ServerHandler(Map<String, Object> serviceBeanMap) {
        this.serviceBeanMap = serviceBeanMap;
    }


}
