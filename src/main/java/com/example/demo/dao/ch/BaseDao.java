package com.example.demo.dao.ch;

import com.example.demo.common.dao.GenericDao;
import com.example.demo.config.MongoDataSourceConfig;
import com.example.demo.config.MysqlDataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Slf4j
@PropertySource("classpath:config/dao.properties")
public abstract class BaseDao extends GenericDao {

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_YXZW_TEMPLATE)
    protected JdbcTemplate yxzwJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_JKCT_TEMPLATE)
    protected JdbcTemplate jkctJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_TNB_TEMPLATE)
    protected JdbcTemplate tnbJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_YX_TEMPLATE)
    protected JdbcTemplate yxJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_XZDM_TEMPLATE)
    protected JdbcTemplate xzdmJdbcTemplate;

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    protected MongoTemplate hrsMongoTemplate;

    protected JdbcTemplate getJdbcTemplate(String dataSource) {
        if (MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE.equals(dataSource)) {
            return jkctJdbcTemplate;
        }
        if (MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE.equals(dataSource)) {
            return yxzwJdbcTemplate;
        }
        if (MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE.equals(dataSource)) {
            return tnbJdbcTemplate;
        }
        if (MysqlDataSourceConfig.MYSQL_YX_DATASOURCE.equals(dataSource)) {
            return yxJdbcTemplate;
        }
        if (MysqlDataSourceConfig.MYSQL_XZDM_DATASOURCE.equals(dataSource)) {
            return xzdmJdbcTemplate;
        }
        return jkctJdbcTemplate;
    }

    protected List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        log.debug("findOrgOdCatByGroupRecordName(): 查找诊断名称通过一次就诊号: " + groupRecordName);
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<String> results = jdbcTemplate.queryForList(sql, String.class, groupRecordName);
        return results;
    }

}
