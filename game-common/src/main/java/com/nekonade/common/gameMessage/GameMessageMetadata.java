package com.nekonade.common.gameMessage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameMessageMetadata {
    int messageId(); // 消息请求Id

    int serviceId(); // 服务Id,消息请求的服务Id。

    int groupId() default ConstMessageGroup.NONE;

    EnumMessageType messageType();//消息类型，request和response

//    long activeStartTimestamp();//从此刻开始请求有效
//
//    long expiredTimestamp();//从此刻开始请求无效
}
