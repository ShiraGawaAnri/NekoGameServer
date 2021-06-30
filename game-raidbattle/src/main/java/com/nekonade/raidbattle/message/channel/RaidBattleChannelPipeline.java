package com.nekonade.raidbattle.message.channel;

import com.nekonade.common.concurrent.GameEventExecutorGroup;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.gameMessage.IGameMessage;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class RaidBattleChannelPipeline {
    static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);

    private static final String HEAD_NAME = generateName0(HeadContext.class);
    private static final String TAIL_NAME = generateName0(TailContext.class);

    private static final FastThreadLocal<Map<Class<?>, String>> nameCaches = new FastThreadLocal<Map<Class<?>, String>>() {
        @Override
        protected Map<Class<?>, String> initialValue() throws Exception {
            return new WeakHashMap<Class<?>, String>();
        }
    };

    final AbstractRaidBattleChannelHandlerContext head;
    final AbstractRaidBattleChannelHandlerContext tail;

    private final RaidBattleChannel channel;
    private Map<EventExecutorGroup, EventExecutor> childExecutors;

    protected RaidBattleChannelPipeline(RaidBattleChannel channel) {
        this.channel = ObjectUtil.checkNotNull(channel, "channel");

        tail = new TailContext(this);
        head = new HeadContext(this);

        head.next = tail;
        tail.prev = head;
    }

    private static String generateName0(Class<?> handlerType) {
        return StringUtil.simpleClassName(handlerType) + "#0";
    }

    /**
     *
     * @param group 线程group
     * @param singleEventExecutorPerGroup 如果为true，那么多个不同的Handler如果使用同一个GameEventExecutorGroup中选择EventExecutor，在调用handler里面的方法时，都是使用的同
     * @param name
     * @param handler
     * @return
     */
    private AbstractRaidBattleChannelHandlerContext newContext(GameEventExecutorGroup group, boolean singleEventExecutorPerGroup, String name, RaidBattleChannelHandler handler) {
        return new DefaultRaidBattleChannelHandlerContext(this, childExecutor(group, singleEventExecutorPerGroup), name, handler);
    }

    private EventExecutor childExecutor(GameEventExecutorGroup group, boolean singleEventExecutorPerGroup) {
        if (group == null) {
            return null;
        }

        if (!singleEventExecutorPerGroup) {
            return group.next();
        }
        Map<EventExecutorGroup, EventExecutor> childExecutors = this.childExecutors;
        if (childExecutors == null) {
            // Use size of 4 as most people only use one extra EventExecutor.
            childExecutors = this.childExecutors = new IdentityHashMap<EventExecutorGroup, EventExecutor>(4);
        }
        // Pin one of the child executors once and remember it so that the same child executor
        // is used to fire events for the same channel.
        EventExecutor childExecutor = childExecutors.get(group);
        if (childExecutor == null) {
            childExecutor = group.next();
            childExecutors.put(group, childExecutor);
        }
        return childExecutor;
    }

    public final RaidBattleChannel gameChannel() {
        return channel;
    }

    public final RaidBattleChannelPipeline addFirst(String name, boolean singleEventExecutorPerGroup, RaidBattleChannelHandler handler) {
        return addFirst(null, singleEventExecutorPerGroup, name, handler);
    }

    public final RaidBattleChannelPipeline addFirst(GameEventExecutorGroup group, boolean singleEventExecutorPerGroup, String name, RaidBattleChannelHandler handler) {
        final AbstractRaidBattleChannelHandlerContext newCtx;
        synchronized (this) {
            name = filterName(name, handler);
            newCtx = newContext(group, singleEventExecutorPerGroup, name, handler);
            addFirst0(newCtx);
        }
        return this;
    }

    private void addFirst0(AbstractRaidBattleChannelHandlerContext newCtx) {
        AbstractRaidBattleChannelHandlerContext nextCtx = head.next;
        newCtx.prev = head;
        newCtx.next = nextCtx;
        head.next = newCtx;
        nextCtx.prev = newCtx;
    }

    public final RaidBattleChannelPipeline addLast(boolean singleEventExecutorPerGroup, String name, RaidBattleChannelHandler handler) {
        return addLast(null, singleEventExecutorPerGroup, name, handler);
    }

    public final RaidBattleChannelPipeline addLast(GameEventExecutorGroup group, boolean singleEventExecutorPerGroup, String name, RaidBattleChannelHandler handler) {
        final AbstractRaidBattleChannelHandlerContext newCtx;
        synchronized (this) {
            newCtx = newContext(group, singleEventExecutorPerGroup, filterName(name, handler), handler);
            addLast0(newCtx);
        }
        return this;
    }

    private void addLast0(AbstractRaidBattleChannelHandlerContext newCtx) {
        AbstractRaidBattleChannelHandlerContext prev = tail.prev;
        newCtx.prev = prev;
        newCtx.next = tail;
        prev.next = newCtx;
        tail.prev = newCtx;
    }

    private String filterName(String name, RaidBattleChannelHandler handler) {
        if (name == null) {
            return generateName(handler);
        }
        checkDuplicateName(name);
        return name;
    }

    public final RaidBattleChannelPipeline addFirst(RaidBattleChannelHandler... handlers) {
        return addFirst(null, false, handlers);
    }

    public final RaidBattleChannelPipeline addFirst(GameEventExecutorGroup executor, boolean singleEventExecutorPerGroup, RaidBattleChannelHandler... handlers) {
        if (handlers == null) {
            throw new NullPointerException("handlers");
        }
        if (handlers.length == 0 || handlers[0] == null) {
            return this;
        }

        int size;
        for (size = 1; size < handlers.length; size++) {
            if (handlers[size] == null) {
                break;
            }
        }

        for (int i = size - 1; i >= 0; i--) {
            RaidBattleChannelHandler h = handlers[i];
            addFirst(executor, singleEventExecutorPerGroup, null, h);
        }

        return this;
    }

    public final RaidBattleChannelPipeline addLast(RaidBattleChannelHandler... handlers) {
        return addLast(null, false, handlers);
    }

    public final RaidBattleChannelPipeline addLast(GameEventExecutorGroup executor, boolean singleEventExecutorPerGroup, RaidBattleChannelHandler... handlers) {
        if (handlers == null) {
            throw new NullPointerException("handlers");
        }

        for (RaidBattleChannelHandler h : handlers) {
            if (h == null) {
                break;
            }
            addLast(executor, false, null, h);
        }

        return this;
    }

    private String generateName(RaidBattleChannelHandler handler) {
        Map<Class<?>, String> cache = nameCaches.get();
        Class<?> handlerType = handler.getClass();
        String name = cache.get(handlerType);
        if (name == null) {
            name = generateName0(handlerType);
            cache.put(handlerType, name);
        }

        // It's not very likely for a user to put more than one handler of the same type, but make sure to
        // avoid
        // any name conflicts. Note that we don't cache the names generated here.
        if (context0(name) != null) {
            String baseName = name.substring(0, name.length() - 1); // Strip the trailing '0'.
            for (int i = 1; ; i++) {
                String newName = baseName + i;
                if (context0(newName) == null) {
                    name = newName;
                    break;
                }
            }
        }
        return name;
    }

    public final RaidBattleChannelPipeline fireRegister(String raidId, RaidBattleChannelPromise promise) {
        AbstractRaidBattleChannelHandlerContext.invokeChannelRegistered(head, raidId, promise);
        return this;
    }

    public final RaidBattleChannelPipeline fireChannelInactive() {
        AbstractRaidBattleChannelHandlerContext.invokeChannelInactive(head);
        return this;
    }

    public final RaidBattleChannelPipeline fireChannelReadRPCRequest(IGameMessage gameMessage) {
        AbstractRaidBattleChannelHandlerContext.invokeChannelReadRPCRequest(head, gameMessage);
        return this;
    }

    public final RaidBattleChannelPipeline fireExceptionCaught(Throwable cause) {
        AbstractRaidBattleChannelHandlerContext.invokeExceptionCaught(head, cause);
        return this;
    }

    public final RaidBattleChannelPipeline fireUserEventTriggered(Object event, Promise<Object> promise) {
        AbstractRaidBattleChannelHandlerContext.invokeUserEventTriggered(head, event, promise);
        return this;
    }

    public final RaidBattleChannelPipeline fireChannelRead(Object msg) {
        AbstractRaidBattleChannelHandlerContext.invokeChannelRead(head, msg);
        return this;
    }

    public final RaidBattleChannelFuture close() {
        return tail.close(new DefaultRaidBattleChannelPromise(this.channel));
    }


    public final RaidBattleChannelFuture writeAndFlush(IGameMessage msg, RaidBattleChannelPromise promise) {
        return tail.writeAndFlush(msg, promise);
    }

    public final RaidBattleChannelFuture writeAndFlush(IGameMessage msg) {
        return tail.writeAndFlush(msg);
    }

    public final void writeRpcRequest(IGameMessage gameMessage, Promise<IGameMessage> promise) {
        tail.writeRPCMessage(gameMessage, promise);
    }

    private void checkDuplicateName(String name) {
        if (context0(name) != null) {
            throw new IllegalArgumentException("Duplicate handler name: " + name);
        }
    }

    private AbstractRaidBattleChannelHandlerContext context0(String name) {
        AbstractRaidBattleChannelHandlerContext context = head.next;
        while (context != tail) {
            if (context.name().equals(name)) {
                return context;
            }
            context = context.next;
        }
        return null;
    }

    /**
     * Called once a {@link Throwable} hit the end of the {@link ChannelPipeline} without been handled
     * by the user in {@link ChannelHandler#exceptionCaught(ChannelHandlerContext, Throwable)}.
     */
    protected void onUnhandledInboundException(Throwable cause) {
        try {
            logger.warn("An exceptionCaught() event was fired, and it reached at the tail of the pipeline. " + "It usually means the last handler in the pipeline did not handle the exception.", cause);
        } finally {
            ReferenceCountUtil.release(cause);
        }
    }

    /**
     * Called once a message hit the end of the {@link ChannelPipeline} without been handled by the user
     * in {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}. This method is
     * responsible to call {@link ReferenceCountUtil#release(Object)} on the given msg at some point.
     */
    protected void onUnhandledInboundMessage(Object msg) {
        try {
            logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. " + "Please check your pipeline configuration.", msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    // A special catch-all handler that handles both bytes and messages.
    final class TailContext extends AbstractRaidBattleChannelHandlerContext implements RaidBattleChannelInboundHandler {

        TailContext(RaidBattleChannelPipeline pipeline) {
            super(pipeline, null, TAIL_NAME, true, false);

        }

        @Override
        public RaidBattleChannelHandler handler() {
            return this;
        }

        @Override
        public void channelInactive(AbstractRaidBattleChannelHandlerContext ctx) throws Exception {

        }


        @Override
        public void userEventTriggered(AbstractRaidBattleChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {

        }

        @Override
        public void exceptionCaught(AbstractRaidBattleChannelHandlerContext ctx, Throwable cause) throws Exception {
            onUnhandledInboundException(cause);
        }

        @Override
        public void channelRead(AbstractRaidBattleChannelHandlerContext ctx, Object msg) throws Exception {
            onUnhandledInboundMessage(msg);
        }

        @Override
        public void channelRegister(AbstractRaidBattleChannelHandlerContext ctx, String raidId, RaidBattleChannelPromise promise) {
            promise.setSuccess();
            logger.debug("注册事件未处理");
        }

        @Override
        public void channelReadRPCRequest(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage msg) throws Exception {
            onUnhandledInboundMessage(msg);
        }
    }

    final class HeadContext extends AbstractRaidBattleChannelHandlerContext implements RaidBattleChannelOutboundHandler, RaidBattleChannelInboundHandler {

        HeadContext(RaidBattleChannelPipeline pipeline) {
            super(pipeline, null, HEAD_NAME, false, true);

        }

        @Override
        public RaidBattleChannelHandler handler() {
            return this;
        }

        @Override
        public void writeAndFlush(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage gameMessage, RaidBattleChannelPromise promise) throws Exception {
            GameMessagePackage gameMessagePackage = new GameMessagePackage();
            GameMessageHeader header = gameMessage.getHeader().clone();
            //重新设置raidId，防止不同channel之间由于使用同一个IGameMessage实例，相互覆盖
            header.getAttribute().setRaidId(channel.getRaidId());
            header.setToServerId(channel.getGatewayServerId());
            header.setFromServerId(channel.getServerConfig().getServerId());
            header.setServerSendTime(System.currentTimeMillis());
            gameMessagePackage.setHeader(header);
            gameMessagePackage.setBody(gameMessage.body());
            channel.unsafeSendMessage(gameMessagePackage, promise);// 调用GameChannel的方法，向外部发送消息
        }

        @Override
        public void exceptionCaught(AbstractRaidBattleChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.fireExceptionCaught(cause);
        }


        @Override
        public void channelInactive(AbstractRaidBattleChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelInactive();
        }

        @Override
        public void channelRead(AbstractRaidBattleChannelHandlerContext ctx, Object msg) throws Exception {
            ctx.fireChannelRead(msg);
        }

        @Override
        public void userEventTriggered(AbstractRaidBattleChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
            ctx.fireUserEventTriggered(evt, promise);
        }


        @Override
        public void channelRegister(AbstractRaidBattleChannelHandlerContext ctx, String raidId, RaidBattleChannelPromise promise) {
            ctx.fireChannelRegistered(raidId, promise);
        }

        @Override
        public void close(AbstractRaidBattleChannelHandlerContext ctx, RaidBattleChannelPromise promise) {
            channel.unsafeClose();
        }

        @Override
        public void writeRPCMessage(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage gameMessage, Promise<IGameMessage> callback) {
            channel.unsafeSendRpcMessage(gameMessage, callback);
        }

        @Override
        public void channelReadRPCRequest(AbstractRaidBattleChannelHandlerContext ctx, IGameMessage msg) throws Exception {
            ctx.fireChannelReadRPCRequest(msg);
        }


    }
}
