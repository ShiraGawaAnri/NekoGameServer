package com.nekonade.dao.daos;

import com.nekonade.common.concurrent.GameEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAsyncDao {
    private final GameEventExecutorGroup executorGroup;
    protected Logger logger = null;

    public AbstractAsyncDao(GameEventExecutorGroup executorGroup) {
        this.executorGroup = executorGroup;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected void execute(long playerId, Promise<?> promise, Runnable task) {
        EventExecutor executor = this.executorGroup.select(playerId);
        executor.execute(() -> {
            try {
                task.run();
            } catch (Throwable e) {// 统一进行异常捕获，防止由于数据库查询的异常导到线程卡死
                logger.error("数据库操作失败,playerId:{}", playerId, e);
                if (promise != null) {
                    promise.setFailure(e);
                }
            }
        });
    }

    protected void execute(String selectKey, Promise<?> promise, Runnable task) {
        EventExecutor executor = this.executorGroup.select(selectKey);
        executor.execute(() -> {
            try {
                task.run();
            } catch (Throwable e) {// 统一进行异常捕获，防止由于数据库查询的异常导到线程卡死
                logger.error("数据库操作失败,selectKey:{}", selectKey, e);
                if (promise != null) {
                    promise.setFailure(e);
                }
            }
        });
    }

    protected EventExecutor getEventExecutor(String selectKey) {
        return this.executorGroup.select(selectKey);
    }

}
