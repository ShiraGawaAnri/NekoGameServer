package com.nekonade.common.concurrent;

import io.netty.util.concurrent.*;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class GameEventExecutorGroup extends AbstractEventExecutorGroup {
    static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Math.max(16,
            SystemPropertyUtil.getInt("io.netty.eventexecutor.maxPendingTasks", Integer.MAX_VALUE));
    private final EventExecutor[] children;
    private final AtomicInteger childIndex = new AtomicInteger();
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    @SuppressWarnings("rawtypes")
    private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);

    /**
     * @see #GameEventExecutorGroup(int, ThreadFactory)
     */
    public GameEventExecutorGroup(int nThreads) {
        this(nThreads, null);
    }

    /**
     * Create a new instance.
     *
     * @param nThreads      the number of threads that will be used by this instance.
     * @param threadFactory the ThreadFactory to use, or {@code null} if the default should be used.
     */
    public GameEventExecutorGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, DEFAULT_MAX_PENDING_EXECUTOR_TASKS, RejectedExecutionHandlers.reject());
    }

    /**
     * Create a new instance.
     *
     * @param nThreads        the number of threads that will be used by this instance.
     * @param threadFactory   the ThreadFactory to use, or {@code null} if the default should be used.
     * @param maxPendingTasks the maximum number of pending tasks before new tasks will be rejected.
     * @param rejectedHandler the {@link RejectedExecutionHandler} to use.
     */
    public GameEventExecutorGroup(int nThreads, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }
        if (threadFactory == null) {
            threadFactory = newDefaultThreadFactory();
        }
        children = new SingleThreadEventExecutor[nThreads];
        for (int i = 0; i < nThreads; i++) {
            boolean success = false;
            try {
                children[i] = newChild(threadFactory, maxPendingTasks, rejectedHandler);
                success = true;
            } catch (Exception e) {
                // TODO: Think about if this is a good exception type
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                if (!success) {
                    for (int j = 0; j < i; j++) {
                        children[j].shutdownGracefully();
                    }

                    for (int j = 0; j < i; j++) {
                        EventExecutor e = children[j];
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
        final FutureListener<Object> terminationListener = new FutureListener<Object>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (terminatedChildren.incrementAndGet() == children.length) {
                    terminationFuture.setSuccess(null);
                }
            }
        };
        for (EventExecutor e : children) {
            e.terminationFuture().addListener(terminationListener);
        }
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultThreadFactory(getClass());
    }

    @Override
    public EventExecutor next() {
        return this.getEventExecutor(childIndex.getAndIncrement());
    }

    public EventExecutor select(Object selectKey) {
        if (selectKey == null) {
            throw new IllegalArgumentException("selectKey不能为空");
        }
        int hashCode = selectKey.hashCode();
        return this.getEventExecutor(hashCode);
    }

    public <T> Future<T> submit(Object selectKey, Callable<T> task) {
        return this.select(selectKey).submit(task);
    }

    public Future<?> submit(Object selectKey, Runnable task) {
        return this.select(selectKey).submit(task);
    }

    public void execute(Object selectKey, Runnable command) {
        this.select(selectKey).execute(command);
    }

    public <V> ScheduledFuture<V> schedule(Object selectKey, Callable<V> callable, long delay, TimeUnit unit) {
        return this.select(selectKey).schedule(callable, delay, unit);
    }

    public ScheduledFuture<?> schedule(Object selectKey, Runnable command, long delay, TimeUnit unit) {
        return this.select(selectKey).schedule(command, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Object selectKey, Runnable command, long initialDelay, long period, TimeUnit unit) {
        return this.select(selectKey).scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Object selectKey, Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return this.select(selectKey).scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    private EventExecutor getEventExecutor(int value) {
        if (isPowerOfTwo(this.children.length)) {
            return children[value & children.length - 1];
        } else {
            return children[Math.abs(value % children.length)];
        }
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return children().iterator();
    }

    /**
     * Return the number of {@link EventExecutor} this implementation uses. This number is the maps 1:1
     * to the threads it use.
     */
    public final int executorCount() {
        return children.length;
    }

    /**
     * Return a safe-copy of all of the children of this group.
     */
    protected Set<EventExecutor> children() {
        Set<EventExecutor> children = Collections.newSetFromMap(new LinkedHashMap<EventExecutor, Boolean>());
        Collections.addAll(children, this.children);
        return children;
    }

    /**
     * Create a new EventExecutor which will later then accessible via the {@link #next()} method. This
     * method will be called for each thread that will serve this {@link MultithreadEventExecutorGroup}.
     */
    protected EventExecutor newChild(ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedHandler) throws Exception {
        return new DefaultEventExecutor(this, threadFactory, maxPendingTasks, rejectedHandler);
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        for (EventExecutor l : children) {
            l.shutdownGracefully(quietPeriod, timeout, unit);
        }
        return terminationFuture();
    }

    @Override
    public Future<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    public boolean isShuttingDown() {
        for (EventExecutor l : children) {
            if (!l.isShuttingDown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isShutdown() {
        for (EventExecutor l : children) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        for (EventExecutor l : children) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        loop:
        for (EventExecutor l : children) {
            for (; ; ) {
                long timeLeft = deadline - System.nanoTime();
                if (timeLeft <= 0) {
                    break loop;
                }
                if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
                    break;
                }
            }
        }
        return isTerminated();
    }

    @Override
    public void shutdown() {
        this.shutdownGracefully();
    }

    @Override
    public void execute(Runnable command) {
        try{
            super.execute(command);
        }catch (Throwable e){
            throw e;
        }
    }
}
