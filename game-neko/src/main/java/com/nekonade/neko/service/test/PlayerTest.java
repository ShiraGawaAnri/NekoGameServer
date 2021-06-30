package com.nekonade.neko.service.test;

import com.nekonade.dao.daos.PlayerDao;
import com.nekonade.dao.db.entity.Player;
import io.netty.util.concurrent.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PlayerTest {
    private final EventExecutorGroup dbExecutorGroup = new DefaultEventExecutorGroup(4);//声明一个数据库线程池组
    @Autowired
    private PlayerDao playerDao;//注入数据库操作类

    public static void main(String[] args) {
        Player player = new Player();
        player.setPlayerId(1);
        PlayerService playerService = new PlayerService();
        playerService.addPlayer(player);//模拟在缓存中添加一个用户
        AtomicInteger count = new AtomicInteger();
        Thread t1 = new Thread(() -> {
            while (count.get() < 10000) {
                Player p = playerService.getPlayer(1L);
                p.getMap().forEach((k, v) -> {
                    System.out.println(v);
                });
            }
        });//模拟一个用户在线程1遍历Player中的Map
        t1.start();
        Thread t2 = new Thread(() -> {
            while (count.get() < 10000) {
                int index = count.incrementAndGet();
                Player p = playerService.getPlayer(1L);
                p.getMap().put("a" + index, index);//模拟一个用户在线程修改Map
            }
        });
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Player queryPlayer(long playerId) throws InterruptedException, ExecutionException {
        Future<Player> future = dbExecutorGroup.next().submit(() -> {
            Player player = playerDao.findById(playerId).orElse(null);
            return player;
        });
        Player player = future.get();//等待返回查询结果
        return player;
    }

    public void queryPlayer(long playerId, Consumer<Player> conumer) {
        dbExecutorGroup.next().execute(() -> {
            Player player = playerDao.findById(playerId).orElse(null);
            conumer.accept(player);
        });
    }

    public void test() {//这个方法测试获取Player并操作Player
        this.queryPlayer(1L, player -> {//通过回调方式获取Player
            if (player != null) {
                //对Player进行其它操作
                player.getMap().forEach((k, v) -> {
                    System.out.println(k + "-" + v);
                });
            }
        });
    }

    public Future<Player> queryPlayer(Long playerId, Promise<Player> promise) {
        dbExecutorGroup.next().execute(() -> {
            Player player = playerDao.findById(playerId).orElse(null);
            promise.setSuccess(player);
        });
        return promise;
    }

    public void futureTest() {
        EventExecutor executor = new DefaultEventExecutor();
        executor.execute(() -> {
            Promise<Player> promise = new DefaultPromise<>(executor);
            queryPlayer(1L, promise).addListener((GenericFutureListener<Future<Player>>) future -> {
                if (future.isSuccess()) {
                    Player player = future.get();
                    //对Player进行其它操作
                    player.getMap().forEach((k, v) -> {
                        System.out.println(k + "-" + v);
                    });
                }
            });
        });

    }


}
