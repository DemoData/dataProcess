package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class MysqlDataSourceConfig {

    public static final String MYSQL_YXZW_PREFIX = "mysql.yixianzhanwei";
    public static final String MYSQL_JKCT_PREFIX = "mysql.jiankangchati";
    public static final String MYSQL_YXZW_DATASOURCE = "yxzwDataSource";
    public static final String MYSQL_JKCT_DATASOURCE = "jkctDataSource";
    public static final String MYSQL_YXZW_TEMPLATE = "yxzwJdbcTemplate";
    public static final String MYSQL_JKCT_TEMPLATE = "jkctJdbcTemplate";

    @Primary
    @Bean(name = MYSQL_YXZW_DATASOURCE)
    @Qualifier(MYSQL_YXZW_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_YXZW_PREFIX)
    public DataSource yxzwDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_JKCT_DATASOURCE)
    @Qualifier(MYSQL_JKCT_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_JKCT_PREFIX)
    public DataSource jkctDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_YXZW_TEMPLATE)
    public JdbcTemplate yxzwJdbcTemplate(@Qualifier(MYSQL_YXZW_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = MYSQL_JKCT_TEMPLATE)
    public JdbcTemplate jkctJdbcTemplate(@Qualifier(MYSQL_JKCT_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }
}
