package com.example.demo.service.ch.xgwk;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.TextDao;
import com.example.demo.dao.standard.IMedicalHistoryDao;
import com.example.demo.entity.MedicalHistory;
import com.example.demo.entity.Record;
import com.example.demo.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chxgMedicalHistoryService")
public class XGMedicalHistoryServiceImpl extends TextService<MedicalHistory> {

    @Autowired
    @Qualifier("xgMedicalHistoryDao")
    IMedicalHistoryDao medicalHistoryDao;

    @Override
    protected TextDao<MedicalHistory> currentDao() {
        return medicalHistoryDao;
    }

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        //通過mapping set对应的类型
        setRecordType(record, entity);

        String orgOdCategoriesStr = entity.getOrgOdCategories();
        if (StringUtils.isEmpty(orgOdCategoriesStr)) {
            log.error("customProcess(): orgOdCategoriesStr is null,id:" + entity.toString());
            return;
        }
        String[] orgOdCategories = orgOdCategoriesStr.split(",");
        Map<String, String> orgMap = new HashMap<>();
        for (String orgOdCategory : orgOdCategories) {
            String org = orgOdCategory.indexOf("（") > 0 ? orgOdCategory.substring(0, orgOdCategory.indexOf("（")) : orgOdCategory;
            orgMap.put(org, null);
        }
        List<String> recordOrgOd = new ArrayList<>();
        for (Map.Entry<String, String> entry : orgMap.entrySet()) {
            recordOrgOd.add(entry.getKey());
        }
        record.setOrgOdCategories(recordOrgOd.toArray(new String[0]));
    }

    @Override
    protected Map<String, String> getFormattedText(MedicalHistory entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        return null;
    }

    @Override
    protected void initRecordBasicInfo(Record record, MedicalHistory medicalHistory) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setBatchNo("shch20180315");
        record.setDepartment("血管外科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setPatientId("shch_" + medicalHistory.getPatientId());
        record.setGroupRecordName(medicalHistory.getGroupRecordName());
        record.setSourceId(medicalHistory.getId().toString());
    }

    /**
     * Put MedicalHistory data to Record
     *
     * @param medicalHistory
     * @param record
     */
    protected void putText2Record(MedicalHistory medicalHistory, Record record) {
        JSONObject info = record.getInfo();
        String medicalContent = medicalHistory.getMedicalContent();
        if (StringUtils.isEmpty(medicalContent)) {
            log.error("!!!! 病历内容为空 , id : " + medicalHistory.getId() + "!!!!");
        }
        info.put(TextFormatter.TEXT, medicalContent);
        info.put(TextFormatter.TEXT_ARS, medicalContent);
    }

    private void setRecordType(Record record, MedicalHistory medicalHistory) {
        String typeName = medicalHistory.getMedicalHistoryName();
        String[] types = typeName.split("-");
        record.setRecordType(types[0]);
        record.setSubRecordType(types[1]);
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
