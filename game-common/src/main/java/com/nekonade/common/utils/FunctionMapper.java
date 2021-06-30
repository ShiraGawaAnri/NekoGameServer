package com.nekonade.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class FunctionMapper {

    public static <T, R> Function<T, R> Mapper(Class<T> tClass, Class<R> rClass) {
        return source -> {
            R r = null;
            try {
                r = rClass.getDeclaredConstructor().newInstance();
                Field[] fields = rClass.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String name = field.getName();
                    String simpleName = field.getType().getSimpleName();
                    try {
                        Field getField = tClass.getDeclaredField(name);
                        getField.setAccessible(true);
                        if (getField.getType().getSimpleName().equals(simpleName)) {
                            field.set(r, getField.get(source));
                        }
                    } catch (NoSuchFieldException ignored) {
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
            return r;
        };
    }
}
