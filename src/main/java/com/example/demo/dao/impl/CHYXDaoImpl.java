package com.example.demo.dao.impl;

import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.dao.ICHYXDao;
import com.example.model.MedicalContentSplitModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
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

    @Override
    public List<Map<String, Object>> findMedicalContentCountMap() {
        return jkctJdbcTemplate.queryForList("select 一次就诊号, 病历内容, count(*) num from 病历文书 group by 一次就诊号,病历内容 HAVING num > 1 ");
    }

    @Override
    public List<Map<String, Object>> findCreateDateMedicalNameMapByVisitNumberAndMedicalContent(String visitNumber, String medicalContent) {
        return jkctJdbcTemplate.queryForList("select 创建日期,病历名称 from 病历文书 where 一次就诊号 = '" + visitNumber + "' and 病历内容 = '" + medicalContent.replaceAll("\\\\", "\\\\\\\\\\\\").replaceAll("'","\\\\'") + "'");
    }

    @Override
    public Map<String, Object> findByVisitNumberAndMedicalContentLimitOne(String visitNumber, String medicalContent) {
        return jkctJdbcTemplate.queryForMap("select * from 病历文书 where 一次就诊号 = '" + visitNumber + "' and 病历内容 = '" + medicalContent.replaceAll("\\\\", "\\\\\\\\\\\\").replaceAll("'","\\\\'") + "' limit 1");
    }

    @Override
    public int update(MedicalContentSplitModel medicalContentSplitModel) {
        return jkctJdbcTemplate.update("update 病历文书 SET 更新内容 = '" + medicalContentSplitModel.getMedicalContent().replaceAll("\\\\", "\\\\\\\\").replaceAll("'","\\\\'") + "' where 一次就诊号='" + medicalContentSplitModel.getVisitNumber() + "' and 创建日期='" + medicalContentSplitModel.getCreateDate() + "'");
    }

    @Override
    public int forbid(MedicalContentSplitModel medicalContentSplitModel) {
        StringBuilder sb = new StringBuilder();
        sb.append("update 病历文书 SET status = 1 where 一次就诊号 = '" + medicalContentSplitModel.getVisitNumber() + "' ");
        if(StringUtils.isNotBlank(medicalContentSplitModel.getCreateDate())){
            sb.append(" and 创建日期 = '" + medicalContentSplitModel.getCreateDate() + "' ");
        }
        if(StringUtils.isNotBlank(medicalContentSplitModel.getMedicalContent())){
            sb.append(" and 病历内容 = '" + medicalContentSplitModel.getMedicalContent().replaceAll("\\\\", "\\\\\\\\\\\\").replaceAll("'","\\\\'") + "'");
        }
        return jkctJdbcTemplate.update(sb.toString());
    }

    @Override
    public void add(Map<String, Object> map) {
        StringBuilder keySb = new StringBuilder();
        StringBuilder valueSb = new StringBuilder();
        for(String key : map.keySet()){
            Object value = map.get(key);
            keySb.append("`" + key + "`");
            keySb.append(",");
            if(value instanceof String) {
                valueSb.append("'" + ((String)value).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'");
            }else{
                valueSb.append(value);
            }
            valueSb.append(",");
        }
        jkctJdbcTemplate.execute("insert into 病历文书(" + keySb.substring(0, keySb.length() - 1) + ") values (" + valueSb.substring(0, valueSb.length() - 1) + ")" );
    }


    public static void main(String[] args) {
        System.out.println("test'''fadfa".replaceAll("'","\\\\'"));
    }

}