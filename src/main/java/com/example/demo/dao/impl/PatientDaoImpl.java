package com.example.demo.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.SqlServerDataSourceConfig;
import com.example.demo.dao.BaseDao;
import com.example.demo.dao.standard.IPatientDao;
import com.example.demo.entity.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author aron
 */
@Slf4j
@Repository("patientDao")
public class PatientDaoImpl extends BaseDao implements IPatientDao {

    @Override
    public List<Patient> findPatients(String dataSource, int pageNum, int pageSize) {
        log.info(">>>>>>>>>>>Searching patients from : " + dataSource + "<<<<<<<<<<<<<<<");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        if (SqlServerDataSourceConfig.SQL_SERVER_DATASOURCE.equals(dataSource)) {
            return super.queryForListInSqlServer(jdbcTemplate, pageNum, pageSize, "Patient", null, null);
        }
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
    public Integer getCount(String dataSource) {
        if (SqlServerDataSourceConfig.SQL_SERVER_DATASOURCE.equals(dataSource)) {
            return getJdbcTemplate(dataSource).queryForObject("select count(t.id) from Patient t", Integer.class);
        }
        return getJdbcTemplate(dataSource).queryForObject("select count(t.病人ID号) from `患者基本信息` t", Integer.class);
    }

    @Override
    protected String generateQuerySql() {
//        String sql = "select t.id AS 'id',CONCAT('shch_', t.`病人ID号`) AS 'patientId',t.`性别` AS 'sex',t.`就诊年龄` AS 'age',t.`就诊日期` AS 'clinicDate',CONCAT('',(LEFT(t.`就诊日期`,4) - t.`就诊年龄`)) AS 'birthDay' from `患者基本信息` t group by t.`病人ID号`";
        String sql = "select * from `患者基本信息`";
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
            /*patient.setPatientId("shch_" + rs.getString("病人ID号"));
            patient.setName(rs.getString("姓名"));*/

            patient.setId(rs.getInt("id"));
            patient.setName(rs.getString("Name"));
            patient.setPatientId("bdsz_" + rs.getString("PID"));
            Date birthYear = rs.getDate("Birthyear");
            patient.setBirthDay(birthYear == null ? "" : birthYear.toString().substring(0, 10));
            patient.setOrigin(rs.getString("Origin"));
            patient.setMarriage(rs.getString("Marriage"));
            patient.setAddress(rs.getString("Address"));
            patient.setSex(rs.getString("sex"));

            /*patient.setSex(rs.getString("sex"));
            patient.setAge(rs.getString("age"));
            patient.setClinicDate(rs.getString("clinicDate"));
            patient.setBirthDay(rs.getString("birthDay"));*/
            return patient;
        }

    }
}