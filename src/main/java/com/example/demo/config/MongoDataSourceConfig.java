package com.example.demo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "mongodb")
public class MongoDataSourceConfig {
    public static final String HDPB_MONGO_TEMPLATE = "hdpbMongoTemplate";
    public static final String HRS_MONGO_TEMPLATE = "hrsMongoTemplate";
    public static final String HDP_MONGO_TEMPLATE = "hdpMongoTemplate";
    public static final String SDS_MONGO_TEMPLATE = "sdsMongoTemplate";

    private MongoProperties hrs = new MongoProperties();
    private MongoProperties sds = new MongoProperties();
    private MongoProperties hdp = new MongoProperties();
    private MongoProperties hdpb = new MongoProperties();

    @Primary
    @Bean(name = HRS_MONGO_TEMPLATE)
    public MongoTemplate hrsMongoTemplate() {
        return generateTemplate(this.getHrs());
    }

    @Bean
    @Qualifier(SDS_MONGO_TEMPLATE)
    public MongoTemplate sdsMongoTemplate() {
        return generateTemplate(this.getSds());
    }

    @Bean
    @Qualifier(HDP_MONGO_TEMPLATE)
    public MongoTemplate hdpMongoTemplate() {
        return generateTemplate(this.getHdp());
    }

    @Bean
    @Qualifier(HDPB_MONGO_TEMPLATE)
    public MongoTemplate hdpbMongoTemplate() {
        return generateTemplate(this.getHdpb());
    }

    private MongoTemplate generateTemplate(MongoProperties mongoProperties) {
        MongoDbFactory mongoDbFactory = generateFactory(mongoProperties);
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory),
                new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(mongoDbFactory, converter);
    }

    private MongoDbFactory generateFactory(MongoProperties mongoProperties) {
        if (log.isInfoEnabled()) {
            log.info(mongoProperties.getDatabase());
        }
        if (StringUtils.isBlank(mongoProperties.getUsername())) {
            return new SimpleMongoDbFactory(new MongoClient(mongoProperties.getHost(), mongoProperties.getPort()),
                    mongoProperties.getDatabase());
        }
        MongoClientOptions.Builder builder = MongoClientOptions.builder().socketTimeout(6 * 60 * 60 * 1000);
        builder.socketKeepAlive(true);
        builder.heartbeatSocketTimeout(30000);
        if (log.isInfoEnabled()) {
            log.info("mongodb://" + mongoProperties.getUsername() + ":" + mongoProperties.getPassword().toString() + "@" + mongoProperties.getHost() + ":" + mongoProperties.getPort() + "/" + mongoProperties.getDatabase());
        }
        MongoClientURI mongoClientURI = new MongoClientURI(
                "mongodb://" + mongoProperties.getUsername() + ":" + new String(mongoProperties.getPassword()) + "@" + mongoProperties.getHost() + ":" + mongoProperties.getPort() + "/" + mongoProperties.getDatabase(), builder);
        if (log.isInfoEnabled()) {
            log.info(mongoClientURI.toString());
        }
        return new SimpleMongoDbFactory(new MongoClient(mongoClientURI),
                mongoProperties.getDatabase());
    }

}
