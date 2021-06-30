package com.nekonade.neko.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataConfigService {

    private final Map<String, Map<String, Object>> dataConfigMap = new HashMap<>();

    @PostConstruct
    public void init() {

    }

    @SuppressWarnings("unchecked")
    public <T> T getDataConfig(String id, Class<T> clazz) {
        String key = clazz.getName();
        Map<String, Object> valueMap = this.dataConfigMap.get(key);
        if (valueMap == null) {
            return null;
        }
        Object value = valueMap.get(id);
        if (value == null) {
            return null;
        }
        return (T) value;
    }
}
