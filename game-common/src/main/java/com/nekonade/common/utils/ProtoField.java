package com.nekonade.common.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface ProtoField {
    boolean Ignore() default false;
    Class TargetClass() default Void.class;
    String TargetRepeatedName() default ""; //非基础类型可以省略
    String Function() default ""; //针对某个特定属性优先执行的方法
}
