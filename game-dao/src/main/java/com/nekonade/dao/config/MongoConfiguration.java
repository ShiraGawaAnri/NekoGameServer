package com.nekonade.dao.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.internal.MongoClientImpl;
import com.nekonade.dao.helper.MongoPageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfiguration {



    @Autowired
    private MyMongoProperties myMongoProperties;

    @Bean
    public MongoProperties properties(MongoProperties properties){
        properties.setAutoIndexCreation(true);
        properties.setDatabase(myMongoProperties.getDatabase());
        properties.setHost(myMongoProperties.getHost());
        properties.setUsername(myMongoProperties.getUsername());
        properties.setPassword(myMongoProperties.getPassword().toCharArray());
        properties.setAuthenticationDatabase(myMongoProperties.getAuthenticationDatabase());
        return properties;
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient){

        SimpleMongoClientDatabaseFactory simpleMongoClientDbFactory = new SimpleMongoClientDatabaseFactory(mongoClient,myMongoProperties.getDatabase());
        MongoTemplate mongoTemplate = new MongoTemplate(simpleMongoClientDbFactory);
        return mongoTemplate;
    }

    @Bean
    public MongoPageHelper mongoPageHelper(MongoTemplate mongoTemplate) {
        return new MongoPageHelper(mongoTemplate);
    }

}
