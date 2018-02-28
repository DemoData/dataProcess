package com.example.demo.com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
public class HRSMongoConfig {
    public static final String MONGO_TEMPLATE = "hrsMongoTemplate";
}
