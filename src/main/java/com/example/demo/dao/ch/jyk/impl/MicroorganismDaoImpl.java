package com.example.demo.dao.ch.jyk.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ch.BaseDao;
import com.example.demo.dao.ch.IMicroorganismDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Microorganism;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class MicroorganismDaoImpl extends BaseDao implements IMicroorganismDao {

    @Override
    public List<Record> findRecord(String dataSource, int PageNum, int PageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), PageNum, PageSize);
    }

    @Override
    public List<Microorganism> findArrayListByCondition(String dataSource, String applyId) {
        log.debug("findMicroorganismByApplyId(): 查找微生物报告通过检验申请号: " + applyId);
        String sql = "select `一次就诊号` AS 'groupRecordName',`检验方法编码` AS 'validateMethodCode',`检验时间` AS 'checkDate',`检验申请号` AS 'checkApplyNo',`微生物代码` AS 'microorganismCode',`微生物培养结果` AS 'microorganismGrowResult',`检验值` AS 'checkValue',`检验结果` AS 'checkResult',`抗生素名称` AS 'antibioticName',`微生物名称` AS 'microorganismName',`项目名称` AS 'projectName',`备注` AS 'remark' from `微生物报告明细` where `检验申请号`=? ";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Microorganism> Microorganisms = jdbcTemplate.query(sql, new BeanPropertyRowMapper(Microorganism.class), applyId);
        return Microorganisms;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`检验申请号`) from `微生物报告明细` t GROUP BY t.`检验申请号`", Integer.class);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        return super.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String groupRecordName) {
        log.debug("findPatientIdByGroupRecordName(): 查找PatientId通过一次就诊号: " + groupRecordName);
        String sql = "select t.`病人ID号` from `患者基本信息` t where t.`一次就诊号`= ? group by t.`一次就诊号`";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        String patientId = jdbcTemplate.queryForObject(sql, String.class, groupRecordName);
        return "shch_" + patientId;
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select t.`一次就诊号` AS 'groupRecordName',t.`检验申请号` AS 'applyId' from `微生物报告明细` t GROUP BY t.`检验申请号` ";
        return sql;
    }

    @Override
    protected RowMapper<Record> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new MicroorganismRowMapper());
        }
        return getRowMapper();
    }

    class MicroorganismRowMapper implements RowMapper<Record> {

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
