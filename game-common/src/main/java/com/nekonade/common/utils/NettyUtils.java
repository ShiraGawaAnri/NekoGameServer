package com.nekonade.common.utils;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class NettyUtils {

    public static String getRemoteIP(Channel channel) {
        InetSocketAddress ipSocket = (InetSocketAddress) channel.remoteAddress();
        String remoteHost = ipSocket.getAddress().getHostAddress();
        return remoteHost;
    }


}
