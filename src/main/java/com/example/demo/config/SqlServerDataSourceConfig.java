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
public class SqlServerDataSourceConfig {

    public static final String SQL_SERVER_PREFIX = "sqlserver.bl";
    public static final String SQL_SERVER_DATASOURCE = "blDataSource";
    public static final String SQL_SERVER_TEMPLATE = "blJdbcTemplate";

    @Bean(name = SQL_SERVER_DATASOURCE)
    @Qualifier(SQL_SERVER_DATASOURCE)
    @ConfigurationProperties(prefix = SQL_SERVER_PREFIX)
    public DataSource blDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = SQL_SERVER_TEMPLATE)
    public JdbcTemplate blJdbcTemplate(@Qualifier(SQL_SERVER_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

}
