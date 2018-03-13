package com.example.demo.service.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.ch.IMedicalHistoryDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.MedicalHistory;
import com.example.demo.service.ch.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxMedicalHistoryService")
public class MedicalHistoryServiceImpl extends BaseService {

    @Autowired
    IMedicalHistoryDao medicalHistoryDao;

    @Override
    protected void process(String dataSource) throws Exception {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCaches = new HashMap<>();
        while (!isFinish) {
            //总共的records
            List<MedicalHistory> medicalHistories = medicalHistoryDao.findMedicalHistoryRecord(dataSource, pageNum, getPageSize());
            if (medicalHistories != null && medicalHistories.size() < getPageSize()) {
                isFinish = true;
            }
            if (medicalHistories == null || medicalHistories.isEmpty()) {
                continue;
            }
            List<JSONObject> jsonList = new ArrayList<>();
            for (MedicalHistory medicalHistory : medicalHistories) {
                Record record = new Record();
                initRecordBasicInfo(record);

                //通過mapping set对应的类型
                setRecordType(record, medicalHistory);

                String groupRecordName = medicalHistory.getGroupRecordName();
                //如果cache中已近存在就不在重复查找
                if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
                    List<String> orgOdCategories = medicalHistoryDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
                    orgOdCatCaches.put(groupRecordName, orgOdCategories);
                }
                record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
                //init odCategories
                record.setOdCategories(new String[]{OD_CATEGORY, this.getOdCategory(dataSource)});
                //put MedicalHistory to Record
                putMedicalHistory2Record(medicalHistory, record);
                JSONObject beanJson = bean2Json(record);
                //不需要id
                beanJson.remove("id");
                beanJson.put("_id", new ObjectId().toString());
                jsonList.add(beanJson);
            }
            if(orgOdCatCaches.size()>50000){
                orgOdCatCaches.clear();
            }
            count += jsonList.size();
            log.info("inserting medicalHistory record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            medicalHistoryDao.batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        //已入库的数据修改入库标识
        medicalHistoryDao.updateStorage(dataSource);
        log.info(">>>>>>>>>>>total inserted medicalHistory records: " + count + " from " + dataSource);
    }

    /**
     * Put MedicalHistory data to Record
     *
     * @param medicalHistory
     * @param record
     */
    private void putMedicalHistory2Record(MedicalHistory medicalHistory, Record record) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        record.setPatientId("shch_" + medicalHistory.getPatientId());
        record.setGroupRecordName(medicalHistory.getGroupRecordName());
        record.setSourceId(medicalHistory.getId().toString());
        record.setSourceRecordType(medicalHistory.getMedicalHistoryName());
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


    /**
     * Set Record basic info
     *
     * @param record
     */
    private void initRecordBasicInfo(Record record) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setUserId("5a7c0adcc2f9c4944dd2b070");
        record.setBatchNo("shch20180309");
        record.setDepartment("检验科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
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
