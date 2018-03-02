package com.example.demo.dao.impl;

import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.dao.ICHYXDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Repository
public class CHYXDaoImpl implements ICHYXDao {

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_YXZW_TEMPLATE)
    protected JdbcTemplate yxzwJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_JKCT_TEMPLATE)
    protected JdbcTemplate jkctJdbcTemplate;


    public void processTest() {
        List<Map<String, Object>> mapList = yxzwJdbcTemplate.queryForList("select distinct 病历名称 from `病历文书`");
        log.debug("=========test========");
        if (log.isInfoEnabled()) {
            log.info(mapList.toString());
        }
    }
}