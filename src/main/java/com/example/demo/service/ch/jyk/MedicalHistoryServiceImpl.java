package com.example.demo.service.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.standard.IMedicalHistoryDao;
import com.example.demo.dao.TextDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.MedicalHistory;
import com.example.demo.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxMedicalHistoryService")
public class MedicalHistoryServiceImpl extends TextService<MedicalHistory> {

    @Autowired
    @Qualifier("jyMedicalHistoryDao")
    IMedicalHistoryDao medicalHistoryDao;

    @Override
    protected TextDao<MedicalHistory> currentDao() {
        return medicalHistoryDao;
    }

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        //通過mapping set对应的类型
        setRecordType(record, entity);

        String groupRecordName = entity.getGroupRecordName();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = medicalHistoryDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
    }

    @Override
    protected Map<String, String> getFormattedText(MedicalHistory entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        return null;
    }

    @Override
    protected void initRecordBasicInfo(Record record, MedicalHistory medicalHistory) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setUserId("5a7c0adcc2f9c4944dd2b070");
        record.setBatchNo("shch20180309");
        record.setDepartment("检验科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setPatientId("shch_" + medicalHistory.getPatientId());
        record.setGroupRecordName(medicalHistory.getGroupRecordName());
        record.setSourceId(medicalHistory.getId().toString());
        record.setSourceRecordType(medicalHistory.getMedicalHistoryName());
    }

    /**
     * Put MedicalHistory data to Record
     *
     * @param medicalHistory
     * @param record
     */
    protected void putText2Record(MedicalHistory medicalHistory, Record record) {
        JSONObject info = record.getInfo();
        //调用提供的方法得到锚点文本
        String medicalContent = medicalHistory.getMedicalContent();
        if (StringUtils.isEmpty(medicalContent)) {
            log.error("!!!! 病历内容为空 , id : " + medicalHistory.getId() + "!!!!");
        }
        String text = TextFormatter.formatTextByAnchaor(medicalContent);
        info.put(TextFormatter.TEXT, text);
        info.put(TextFormatter.TEXT_ARS, medicalContent);
    }

    private void setRecordType(Record record, MedicalHistory medicalHistory) {
        String mapping = medicalHistory.getMapping();
        if (StringUtils.isEmpty(mapping)) {
            log.error("!!!!!!!!!!!! mapping is empty , id : " + medicalHistory.getId() + "!!!!!!!!!!");
            return;
        }
        String[] types = mapping.split("-");
        if (types.length != 2) {
            log.error("!!!!!!!!!!!! mapping value is invalid , id : " + medicalHistory.getId() + "!!!!!!!!!!");
            return;
        }
        record.setRecordType(types[0]);
        record.setSubRecordType(types[1]);
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
