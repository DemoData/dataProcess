package com.example.demo.service.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.ch.IMicroorganismDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Microorganism;
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
@Service("chyxMicroorganismService")
public class MicroorganismServiceImpl extends BaseService {

    @Autowired
    IMicroorganismDao microorganismDao;

    @Override
    protected void process(String dataSource) throws Exception {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCaches = new HashMap<>();
        Map<String, String> patientCaches = new HashMap<>();
        while (!isFinish) {
            //总共的records
            List<Record> recordList = microorganismDao.findMicroorganismRecord(dataSource, pageNum, getPageSize());
            if (recordList != null && recordList.size() < getPageSize()) {
                isFinish = true;
            }
            if (recordList == null || recordList.isEmpty()) {
                continue;
            }
            List<JSONObject> jsonList = new ArrayList<>();
            for (Record record : recordList) {
                String applyId = record.getId();
                initRecordBasicInfo(record);

                String groupRecordName = record.getGroupRecordName();
                //如果cache中已近存在就不在重复查找
                if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
                    List<String> orgOdCategories = microorganismDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
                    orgOdCatCaches.put(groupRecordName, orgOdCategories);
                }
                if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
                    String patientId = microorganismDao.findPatientIdByGroupRecordName(dataSource, groupRecordName);
                    patientCaches.put(groupRecordName, patientId);
                }
                record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
                record.setPatientId(patientCaches.get(groupRecordName));
                //init odCategories
                record.setOdCategories(new String[]{OD_CATEGORY, this.getOdCategory(dataSource)});

                //查找化验报告通过检验申请号,并把找到的值放入info.detailArray
                initInfoArray(record, microorganismDao.findMicroorganismByApplyId(dataSource, applyId));

                JSONObject beanJson = bean2Json(record);
                //不需要id
                beanJson.remove("id");
                beanJson.put("_id", new ObjectId().toString());
                jsonList.add(beanJson);
            }
            if (orgOdCatCaches.size() > 50000) {
                orgOdCatCaches.clear();
            }
            count += jsonList.size();
            log.info("inserting Microorganism record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            microorganismDao.batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted Microorganism records: " + count + " from " + dataSource);
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
        record.setTemplateId(EMPTY_FLAG);
        record.setDepartment("检验科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("微生物");
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

    private void initInfoArray(Record record, List<Microorganism> microorganismList) {
        if (microorganismList == null || microorganismList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (Microorganism microorganism : microorganismList) {
            Map<String, String> map = new HashMap<>();
            map.put(Microorganism.ColumnMapping.GROUP_RECORD_NAME.value(), microorganism.getGroupRecordName() == null ? EMPTY_FLAG : microorganism.getGroupRecordName());
            map.put(Microorganism.ColumnMapping.VALIDATE_METHOD_CODE.value(), microorganism.getValidateMethodCode() == null ? EMPTY_FLAG : microorganism.getValidateMethodCode());
            map.put(Microorganism.ColumnMapping.CHECK_DATE.value(), microorganism.getCheckDate() == null ? EMPTY_FLAG : microorganism.getCheckDate());
            map.put(Microorganism.ColumnMapping.CHECK_APPLY_NO.value(), microorganism.getCheckApplyNo() == null ? EMPTY_FLAG : microorganism.getCheckApplyNo());
            map.put(Microorganism.ColumnMapping.MICROORGANISM_CODE.value(), microorganism.getMicroorganismCode() == null ? EMPTY_FLAG : microorganism.getMicroorganismCode());
            map.put(Microorganism.ColumnMapping.MICROORGANISM_GROW_RESULT.value(), microorganism.getMicroorganismGrowResult() == null ? EMPTY_FLAG : microorganism.getMicroorganismGrowResult());
            map.put(Microorganism.ColumnMapping.CHECK_VALUE.value(), microorganism.getCheckValue() == null ? EMPTY_FLAG : microorganism.getCheckValue());
            map.put(Microorganism.ColumnMapping.CHECK_RESULT.value(), microorganism.getCheckResult() == null ? EMPTY_FLAG : microorganism.getCheckResult());
            map.put(Microorganism.ColumnMapping.ANTIBIOTIC_NAME.value(), microorganism.getAntibioticName() == null ? EMPTY_FLAG : microorganism.getAntibioticName());
            map.put(Microorganism.ColumnMapping.MICROORGANISM_NAME.value(), microorganism.getMicroorganismName() == null ? EMPTY_FLAG : microorganism.getMicroorganismName());
            map.put(Microorganism.ColumnMapping.PROJECT_NAME.value(), microorganism.getProjectName() == null ? EMPTY_FLAG : microorganism.getProjectName());
            map.put(Microorganism.ColumnMapping.REMARK.value(), microorganism.getRemark() == null ? EMPTY_FLAG : microorganism.getRemark());
            detailArray.add(map);
        }
    }

}