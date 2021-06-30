package com.nekonade.network.param.game.messagedispatcher;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service //让此注解继承@Service注解，在项目启动时，自动扫描被GameMessageHandler注解的类
public @interface GameMessageHandler {

}
