package com.nekonade.dao.seq;

import com.nekonade.common.redis.EnumRedisKey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoIncKey {

    String use() default "";

    EnumRedisKey key() default EnumRedisKey.SEQUENCE;

    String id() default "";
}
