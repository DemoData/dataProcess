package com.example.demo.com.example.demo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MultipleMongoConfig {

    @Autowired
    private MultipleMongoProperties multipleMongoProperties;

    @Primary
    @Bean(name = HRSMongoConfig.MONGO_TEMPLATE)
    public MongoTemplate hrsMongoTemplate() {
        return generateTemplate(this.multipleMongoProperties.getHrs());
    }

    @Bean
    @Qualifier(SDSMongoConfig.MONGO_TEMPLATE)
    public MongoTemplate sdsMongoTemplate() {
        return generateTemplate(this.multipleMongoProperties.getSds());
    }

    @Bean
    @Qualifier(HDPMongoConfig.MONGO_TEMPLATE)
    public MongoTemplate hdpMongoTemplate() {
        return generateTemplate(this.multipleMongoProperties.getHdp());
    }

    @Bean
    @Qualifier(HDPBMongoConfig.MONGO_TEMPLATE)
    public MongoTemplate hdpbMongoTemplate() {
        return generateTemplate(this.multipleMongoProperties.getHdpb());
    }

    private MongoTemplate generateTemplate(MongoProperties mongoProperties) {
        MongoDbFactory mongoDbFactory = generateFactory(mongoProperties);
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory),
                new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(mongoDbFactory, converter);
    }

    private MongoDbFactory generateFactory(MongoProperties mongoProperties) {
        System.out.println(mongoProperties.getDatabase());
        if (StringUtils.isBlank(mongoProperties.getUsername())) {
            return new SimpleMongoDbFactory(new MongoClient(mongoProperties.getHost(), mongoProperties.getPort()),
                    mongoProperties.getDatabase());
        }
        MongoClientOptions.Builder builder = MongoClientOptions.builder().socketTimeout(6 * 60 * 60 * 1000);
        builder.socketKeepAlive(true);
        builder.heartbeatSocketTimeout(30000);
        System.out.println("mongodb://" + mongoProperties.getUsername() + ":" + mongoProperties.getPassword().toString() + "@" + mongoProperties.getHost() + ":" + mongoProperties.getPort() + "/" + mongoProperties.getDatabase());
        MongoClientURI mongoClientURI = new MongoClientURI(
                "mongodb://" + mongoProperties.getUsername() + ":" + new String(mongoProperties.getPassword()) + "@" + mongoProperties.getHost() + ":" + mongoProperties.getPort() + "/" + mongoProperties.getDatabase(), builder);
        System.out.println(mongoClientURI.toString());
        return new SimpleMongoDbFactory(new MongoClient(mongoClientURI),
                mongoProperties.getDatabase());
    }
}
