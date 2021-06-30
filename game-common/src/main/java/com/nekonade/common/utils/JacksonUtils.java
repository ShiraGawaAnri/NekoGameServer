package com.nekonade.common.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class JacksonUtils {

    private static final ObjectMapper objectMapper;

    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        objectMapper.setLocale(Locale.CHINESE);
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        //忽略空Bean转json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT,true);
        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));
        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    @SneakyThrows
    public static String toJSONStringV2(Object source){
        return objectMapper.writeValueAsString(source);
    }

    @SneakyThrows
    public static <T> T parseObjectV2(String value, Class<T> clazz){
        return objectMapper.readValue(value,clazz);
    }

    @SneakyThrows
    public static JsonNode toJsonObjectV2(String value){
        return objectMapper.readTree(value);
    }

    public static String toJSONString(Object source){
        return JSON.toJSONString(source);
    }

    public static <T> T parseObject(String value, Class<T> clazz){
        return JSON.parseObject(value,clazz);
    }


}
