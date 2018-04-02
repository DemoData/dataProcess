package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class SqlServerDataSourceConfig {

    public static final String SQL_SERVER = "sqlserver";

    public static final String SQL_SERVER_PREFIX = "sqlserver.zl";
    public static final String SQL_SERVER_DATASOURCE = "sqlserver.zlDataSource";
    public static final String SQL_SERVER_TEMPLATE = "zlJdbcTemplate";

    public static final String SQL_SERVER_FS_PREFIX = "sqlserver.fs";
    public static final String SQL_SERVER_FS_DATASOURCE = "sqlserver.fsDataSource";
    public static final String SQL_SERVER_FS_TEMPLATE = "fsJdbcTemplate";

    public static final String SQL_SERVER_FS_MZ_PREFIX = "sqlserver.fs.mz";
    public static final String SQL_SERVER_FS_MZ_DATASOURCE = "sqlserver.fsmzDataSource";
    public static final String SQL_SERVER_FS_MZ_TEMPLATE = "fsmzJdbcTemplate";

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

    @Bean(name = SQL_SERVER_FS_DATASOURCE)
    @Qualifier(SQL_SERVER_FS_DATASOURCE)
    @ConfigurationProperties(prefix = SQL_SERVER_FS_PREFIX)
    public DataSource fsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = SQL_SERVER_FS_TEMPLATE)
    public JdbcTemplate fsJdbcTemplate(@Qualifier(SQL_SERVER_FS_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = SQL_SERVER_FS_MZ_DATASOURCE)
    @Qualifier(SQL_SERVER_FS_MZ_DATASOURCE)
    @ConfigurationProperties(prefix = SQL_SERVER_FS_MZ_PREFIX)
    public DataSource fsmzDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = SQL_SERVER_FS_MZ_TEMPLATE)
    public JdbcTemplate fsmzJdbcTemplate(@Qualifier(SQL_SERVER_FS_MZ_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

}
