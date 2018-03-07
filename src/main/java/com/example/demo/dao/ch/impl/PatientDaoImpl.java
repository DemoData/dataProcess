package com.example.demo.dao.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.dao.GenericDao;
import com.example.demo.config.MongoDataSourceConfig;
import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.dao.ch.IPatientDao;
import com.example.demo.entity.ch.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author aron
 */
@Slf4j
@Repository("chyxPatientDao")
public class PatientDaoImpl extends GenericDao implements IPatientDao {

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
    public List<Patient> findAllPatients(String dataSource, int pageNum, int pageSize) {
        log.info(">>>>>>>>>>>Searching patients from : " + dataSource + "<<<<<<<<<<<<<<<");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return super.queryForList(jdbcTemplate, pageNum, pageSize);
    }

    @Override
    public JSONObject findPatientByIdInHRS(String pid) {
        Query patientQuery = new Query();
        patientQuery.addCriteria(Criteria.where("_id").is(pid));
        JSONObject patient = hrsMongoTemplate.findOne(patientQuery, JSONObject.class, "Patient");
        return patient;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select t.id AS 'id',CONCAT('shch_', t.`病人ID号`) AS 'patientId',t.`性别` AS 'sex',t.`就诊年龄` AS 'age',t.`就诊日期` AS 'clinicDate',CONCAT('',(LEFT(t.`就诊日期`,4) - t.`就诊年龄`)) AS 'birthDay' from `患者基本信息` t group by t.`病人ID号`";
        return sql;
    }

    @Override
    protected RowMapper<Patient> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new PatientRowMapper());
        }
        return getRowMapper();
    }

    class PatientRowMapper implements RowMapper<Patient> {

        @Override
        public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
            Patient patient = new Patient();
            patient.setId(rs.getString("id"));
            patient.setPatientId(rs.getString("patientId"));
            patient.setSex(rs.getString("sex"));
            patient.setAge(rs.getString("age"));
            patient.setClinicDate(rs.getString("clinicDate"));
            patient.setBirthDay(rs.getString("birthDay"));
            return patient;
        }

    }
}