package com.nekonade.gamegateway.server.handler;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.utils.MessageUtils;
import io.netty.channel.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IdempotenceHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(IdempotenceHandler.class);

    @Getter
    private CaffeineCache cache = new CaffeineCache("message",
            Caffeine.newBuilder().recordStats()
                    .expireAfterWrite(60, TimeUnit.SECONDS)
                    .maximumSize(100)
                    .build());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        GameMessageHeader header = gameMessagePackage.getHeader();
        int messageId = header.getMessageId();
        if(!MessageUtils.getExceptList().contains(messageId)){
            String kafkaKeyCreate = MessageUtils.kafkaKeyCreate(header);
            Cache.ValueWrapper valueWrapper = cache.get(kafkaKeyCreate);
            if(valueWrapper != null){
                logger.info("幂等性拦截->找到已处理过的结果:{}",valueWrapper);
                GameMessagePackage returnPackage = (GameMessagePackage) valueWrapper.get();
                ctx.writeAndFlush(returnPackage);
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        GameMessageHeader header = gameMessagePackage.getHeader();
        int messageId = header.getMessageId();
        if(!MessageUtils.getExceptList().contains(messageId)){
            String kafkaKeyCreate = MessageUtils.kafkaKeyCreate(header);
            cache.put(kafkaKeyCreate,gameMessagePackage);
        }
        super.write(ctx, msg, promise);
    }
}
