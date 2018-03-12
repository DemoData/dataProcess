package com.example.demo.service.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ch.IAssayDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;
import com.example.demo.service.ch.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxAssayService")
public class AssayServiceImpl extends BaseService {

    @Autowired
    IAssayDao assayDao;

    @Override
    protected void process(String dataSource) throws Exception {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCaches = new HashMap<>();
        Map<String, String> patientCaches = new HashMap<>();
        while (!isFinish) {
            List<Record> recordList = assayDao.findAssayRecord(dataSource, pageNum, PAGE_SIZE);
            if (recordList != null && recordList.size() < PAGE_SIZE) {
                isFinish = true;
            }
            if (recordList == null || recordList.isEmpty()) {
                continue;
            }
            List<JSONObject> jsonList = new ArrayList<>();
            //遍历record
            for (Record record : recordList) {
                String applyId = record.getId();
                String groupRecordName = record.getGroupRecordName();
                if (StringUtils.isEmpty(applyId) || StringUtils.isEmpty(groupRecordName)) {
                    continue;
                }
                //init odCategories
                record.setOdCategories(new String[]{OD_CATEGORY, this.getOdCategory(dataSource)});

                //如果cache中已近存在就不在重复查找
                if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
                    List<String> orgOdCategories = assayDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
                    orgOdCatCaches.put(groupRecordName, orgOdCategories);
                }
                if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
                    String patientId = assayDao.findPatientIdByGroupRecordName(dataSource, groupRecordName);
                    patientCaches.put(groupRecordName, patientId);
                }

                record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
                record.setPatientId(patientCaches.get(groupRecordName));

                //查找化验报告通过检验申请号,并把找到的值放入info.detailArray
                initInfoArray(record, assayDao.findAssaysByApplyId(dataSource, applyId));
                jsonList.add(bean2Json(record));
            }
            orgOdCatCaches.clear();
            count += jsonList.size();
            log.info("inserting assay record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            assayDao.batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted assay records: " + count + " from " + dataSource);
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        Record record = (Record) entity;
        //自动生成_id
        JSONObject jsonItem = new JSONObject();
        jsonItem.put("hospitalId", "57b1e21fd897cd373ec7a14f");
        jsonItem.put("userId", "5a7c0adcc2f9c4944dd2b070");
        jsonItem.put("templateId", EMPTY_FLAG);
        jsonItem.put("batchNo","shch20180309");
        jsonItem.put("department", "检验科");
        jsonItem.put("recordType", "化验记录");
        jsonItem.put("subRecordType", "化验");
        jsonItem.put("format", "table");
        jsonItem.put("groupRecordName", record.getGroupRecordName());
        jsonItem.put("patientId", record.getPatientId());
        jsonItem.put("info", record.getInfo());
        jsonItem.put("odCategories", record.getOdCategories());
        jsonItem.put("orgOdCategories", record.getOrgOdCategories());
        jsonItem.put("sourceId", record.getSourceId());
        jsonItem.put("",false);
        jsonItem.put("","采集入库");
        jsonItem.put("","AMD识别完成");
        return jsonItem;
    }


    private void initInfoArray(Record record, List<Assay> assayList) {
        if (assayList == null || assayList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (Assay assay : assayList) {
            Map<String, String> map = new HashMap<>();
            map.put(Assay.ColumnMapping.ASSAY_TIME.value(), assay.getAssayTime());
            map.put(Assay.ColumnMapping.ASSAY_NAME.value(), assay.getAssayName());
            map.put(Assay.ColumnMapping.RESULT_FLAG.value(), assay.getResultFlag());
            map.put(Assay.ColumnMapping.ASSAY_RESULT.value(), assay.getAssayResult());
            map.put(Assay.ColumnMapping.ASSAY_VALUE.value(), assay.getAssayValue());
            map.put(Assay.ColumnMapping.ASSAY_UNIT.value(), assay.getAssayUnit());
            map.put(Assay.ColumnMapping.ASSAY_SPECIMEN.value(), assay.getAssaySpecimen());
            map.put(Assay.ColumnMapping.REFERENCE_RANGE.value(), assay.getReferenceRange());
            map.put(Assay.ColumnMapping.ASSAY_STATE.value(), assay.getAssayState());
            map.put(Assay.ColumnMapping.ASSAY_METHODNAME.value(), assay.getAssayMethodName());
            detailArray.add(map);
        }
    }
}
