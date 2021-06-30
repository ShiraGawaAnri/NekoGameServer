package com.nekonade.gamegateway.server;


import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.common.gameMessage.GameMessagePackage;
import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;

@Service
public class ChannelService {

    private final GameEventExecutorGroup executorGroup = new GameEventExecutorGroup(32);

    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    private final Map<Long, Channel> playerChannelMap = new HashMap<>();// playerId与Netty Channel的映射容器，这里使用的是HashMap，所以，对于Map的操作都要放在锁里面
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();// 读写锁,使用非公平锁


    private final StampedLock stampedLock = new StampedLock();

    // 封装添加读锁，统一添加，防止写错
    private void readLock(Runnable task) {
        lock.readLock().lock();
        try {
            task.run();
        } catch (Exception e) {
            logger.error("ChannelService读锁error");
        } finally {
            lock.readLock().unlock();
        }
    }

    private void writeLock(Runnable task) {// 封装添加写锁，统一添加，防止写错
        lock.writeLock().lock();
        try {
            task.run();
        } catch (Exception e) {  //统一异常捕获
            logger.error("ChannelService写锁处理异常", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addChannel(Long playerId, Channel channel) {
        this.writeLock(() -> {// 数据写入，添加写锁
            playerChannelMap.put(playerId, channel);
        });
    }

    public Channel getChannel(Long playerId) {
        lock.readLock().lock();
        try {
            Channel channel = this.playerChannelMap.get(playerId);
            return channel;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void removeChannel(Long playerId, Channel removedChannel) {
        this.writeLock(() -> {
            Channel existChannel = playerChannelMap.get(playerId);
            if (existChannel != null && existChannel == removedChannel) {// 必须是同一个对象才可以移除
                playerChannelMap.remove(playerId);
                existChannel.close();
            }
        });
    }

    public void broadcast(BiConsumer<Long, Channel> consumer) {// 向Channel广播消息
        this.readLock(() -> {
            this.playerChannelMap.forEach(consumer);
        });
    }

    public void broadcast(GameMessagePackage gameMessage, List<Long> playerIds) {// 向Channel广播消息
        this.readLock(() -> {
            String raidId = gameMessage.getHeader().getAttribute().getRaidId();
            EventExecutor executor = this.executorGroup.select(raidId);
            executor.execute(()->{
                if(playerIds.size() <= 30){
                    playerIds.forEach(id->{
                        Channel channel = this.playerChannelMap.get(id);
                        if(channel != null && channel.isActive()){
                            //logger.info("给playerId {} 发送了消息",id);
                            channel.writeAndFlush(gameMessage);
                        }
                    });
                }else{
                    playerIds.parallelStream().forEach(id->{
                        Channel channel = this.playerChannelMap.get(id);
                        if(channel != null && channel.isActive()){
                            //logger.info("给playerId {} 发送了消息",id);
                            channel.writeAndFlush(gameMessage);
                        }
                    });
                }

            });
        });
    }

    public int getChannelCount() {
        lock.writeLock().lock();
        try {
            int size = this.playerChannelMap.size();// 获取连锁的数量
            return size;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
