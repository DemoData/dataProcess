package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mongodb")
public class MultipleMongoProperties {

    private MongoProperties hrs = new MongoProperties();
    private MongoProperties sds = new MongoProperties();
    private MongoProperties hdp = new MongoProperties();
    private MongoProperties hdpb = new MongoProperties();

    public MongoProperties getHrs() {
        return hrs;
    }

    public void setHrs(MongoProperties hrs) {
        this.hrs = hrs;
    }

    public MongoProperties getSds() {
        return sds;
    }

    public void setSds(MongoProperties sds) {
        this.sds = sds;
    }

    public MongoProperties getHdp() {
        return hdp;
    }

    public void setHdp(MongoProperties hdp) {
        this.hdp = hdp;
    }

    public MongoProperties getHdpb() {
        return hdpb;
    }

    public void setHdpb(MongoProperties hdpb) {
        this.hdpb = hdpb;
    }
}
