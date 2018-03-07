package com.example.demo.dao.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.dao.GenericDao;
import com.example.demo.config.MongoDataSourceConfig;
import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.dao.ch.IAssayDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class AssayDaoImpl extends GenericDao implements IAssayDao {

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
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    protected MongoTemplate hrsMongoTemplate;

    private JdbcTemplate getJdbcTemplate(String dataSource) {
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
        return jkctJdbcTemplate;
    }

    @Override
    public List<Record> findAssayRecord(String dataSource, int PageNum, int PageSize) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return super.queryForList(jdbcTemplate, PageNum, PageSize);
    }

    @Override
    public List<Assay> findAssaysByApplyId(String dataSource, String applyId) {
        log.info("findAssaysByApplyId(): 查找化验报告通过检验申请号: " + applyId);
        String sql = "select t.`检验时间` AS 'assayTime',t.`项目名称` AS 'assayName',t.`结果正常标志` AS 'resultFlag',t.`检验结果` AS 'assayResult',t.`检验值` AS 'assayValue',t.`单位` AS 'assayUnit',t.`标本` AS 'assaySpecimen',t.`参考范围` AS 'referenceRange',t.`检验状态` AS 'assayState',t.`检验方法名称` AS 'assayMethodName' from `检验报告明细` t where t.`检验申请号`=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Assay> assays = jdbcTemplate.query(sql, new BeanPropertyRowMapper(Assay.class), applyId);
        return assays;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        log.info("findOrgOdCatByGroupRecordName(): 查找诊断名称通过一次就诊号: " + groupRecordName);
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<String> results = jdbcTemplate.queryForList(sql, String.class, groupRecordName);
        return results;
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select t.`一次就诊号` AS 'groupRecordName',t.`检验申请号` AS 'applyId' from `检验报告明细` t GROUP BY t.`检验申请号` ";
        return sql;
    }

    @Override
    protected RowMapper<Record> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new AssayRowMapper());
        }
        return getRowMapper();
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setGroupRecordName(rs.getString("groupRecordName"));
            record.setId(rs.getString("applyId"));
            record.setSourceId(rs.getString("applyId"));
            return record;
        }
    }
}
