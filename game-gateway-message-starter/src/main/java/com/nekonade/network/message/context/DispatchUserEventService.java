package com.nekonade.network.message.context;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.BasicException;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.model.ErrorResponseEntity;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.network.message.event.user.BasicEventUser;
import com.nekonade.network.param.game.message.neko.error.GameErrorMsgResponse;
import com.nekonade.network.param.game.message.neko.error.GameNotificationMsgResponse;
import com.nekonade.network.param.game.messagedispatcher.DispatcherMapping;
import com.nekonade.network.param.game.messagedispatcher.GameMessageHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service
public class DispatchUserEventService {

    private static final Logger logger = LoggerFactory.getLogger(DispatchUserEventService.class);

    private final Map<String, DispatcherMapping> userEventMethodCache = new HashMap<>();//数据缓存

    @Autowired
    private ApplicationContext context;//注入spring 上下文类

    @PostConstruct
    public void init() {//项目启动之后，调用此初始化方法
        Map<String, Object> beans = context.getBeansWithAnnotation(GameMessageHandler.class);//从spring 容器中获取所有被@GameMessageHandler标记的所有的类实例
        beans.values().parallelStream().forEach(c -> {//使用stream并行处理遍历这些对象
            Method[] methods = c.getClass().getMethods();
            for (Method method : methods) {//遍历每个类中的方法
                UserEvent userEvent = method.getAnnotation(UserEvent.class);
                if (userEvent != null) {//如果这个方法被@UserEvent注解标记了，缓存下所有的数据
                    String key = userEvent.value().getName();
                    DispatcherMapping dispatcherMapping = new DispatcherMapping(c, method);
                    userEventMethodCache.put(key, dispatcherMapping);
                }
            }
        });
    }

    //通过反射调用处理相应事件的方法
    public void callMethod(UserEventContext<?> ctx, Object event, Promise<Object> promise) {
        String key = event.getClass().getName();
        DispatcherMapping dispatcherMapping = this.userEventMethodCache.get(key);
        if(dispatcherMapping == null && key.equals("io.netty.handler.timeout.IdleStateEvent$DefaultIdleStateEvent")){
            dispatcherMapping = this.userEventMethodCache.get("io.netty.handler.timeout.IdleStateEvent");
        }
        if (dispatcherMapping != null) {
            Object targetObj = dispatcherMapping.getTargetObj();
            try {
                dispatcherMapping.getTargetMethod().invoke(targetObj, ctx, event, promise);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                if(e instanceof InvocationTargetException){
                    Throwable cause = ((InvocationTargetException) e).getTargetException();
                    ErrorResponseEntity errorEntity = new ErrorResponseEntity();
                    BasicException exception;
                    int type = 0;
                    if (cause instanceof GameErrorException) {
                        exception = (GameErrorException) cause;
                        type = 1;
                    } else if (cause instanceof GameNotifyException) {
                        exception = (GameNotifyException) cause;
                        type = 2;
                    } else {
                        exception = GameErrorException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.LogicError).build();
                    }
                    errorEntity.setErrorCode(exception.getError().getErrorCode());
                    errorEntity.setErrorMsg(exception.getError().getErrorDesc());
                    errorEntity.setData(exception.getData());
                    AbstractJsonGameMessage response;
                    switch (type){
                        case 2:
                            response = new GameNotificationMsgResponse();
                            ((GameNotificationMsgResponse)response).getBodyObj().setError(errorEntity);
                            break;
                        default:
                        case 0:
                        case 1:
                            response = new GameErrorMsgResponse();
                            ((GameErrorMsgResponse)response).getBodyObj().setError(errorEntity);
                            break;
                    }
                    if(event instanceof BasicEventUser){
                        BasicEventUser eventUser = (BasicEventUser) event;
                        if(eventUser.request != null){
                            GameMessageHeader header = response.getHeader();
                            GameMessageHeader sourceHeader = eventUser.request.getHeader();
                            header.setClientSendTime(sourceHeader.getClientSendTime());
                            header.setClientSeqId(sourceHeader.getClientSeqId());
                        }
                    }
                    ctx.getCtx().writeAndFlush(response);
                    switch (type){
                        case 1:
                        case 2:
                            return;
                    }
                }
                logger.error("事件处理调用失败，事件对象:{},处理对象：{}，处理方法：{}", event.getClass().getName(), targetObj.getClass().getName(), dispatcherMapping.getTargetMethod().getName(), e);
            }
        } else {
            logger.debug("事件：{} 没有找到处理的方法", event.getClass().getName());
        }
    }

}
