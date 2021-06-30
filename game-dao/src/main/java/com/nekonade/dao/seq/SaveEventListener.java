package com.nekonade.dao.seq;

import com.nekonade.common.redis.EnumRedisKey;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@Component
public class SaveEventListener extends AbstractMongoEventListener<Object> {

    private static final Logger logger = LoggerFactory.getLogger(SaveEventListener.class);

    @Autowired
    private MongoTemplate mongo;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        final Object source = event.getSource();
        ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
            /**
             * Perform an operation using the given field.
             *
             * @param field the field to operate on
             */
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.makeAccessible(field);
                // 如果字段添加了我们自定义的AutoIncKey注解
                if (field.isAnnotationPresent(AutoIncKey.class)
                        //判断注解的字段是否为number类型且值是否等于0.如果大于0说明有ID不需要生成ID
                        && field.get(source) instanceof Number
                        && field.getLong(source) == 0) {
                    // 设置自增ID
                    AutoIncKey annotation = field.getAnnotation(AutoIncKey.class);
                    switch (annotation.use()) {
                        case "":
                        default:
                            field.set(source, getNextId(source.getClass().getCanonicalName()));
                            break;
                        case "redis":
                            field.set(source, getNextIdByRedis(annotation.key(), annotation.id()));
                            break;
                    }
                    logger.info("increase key, source = {} , nextId = {}", source, field.get(source));
                }
            }
        });
    }

    private Long getNextId(String collName) {
        Query query = new Query(Criteria.where("collName").is(collName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        Sequence seq = mongo.findAndModify(query, update, options, Sequence.class);
        return seq.getSeqId();
    }

    private Long getNextIdByRedis(EnumRedisKey enumRedisKey, String id) {
        String key;
        if (StringUtils.isNotEmpty(id)) {
            key = enumRedisKey.getKey(id);
        } else {
            key = enumRedisKey.getKey();
        }
        return stringRedisTemplate.opsForValue().increment(key);
    }
}
