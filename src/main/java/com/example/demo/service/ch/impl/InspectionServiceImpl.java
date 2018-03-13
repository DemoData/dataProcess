package com.example.demo.service.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.ch.IInspectionDao;
import com.example.demo.dao.ch.IMedicalHistoryDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Inspection;
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
@Service("chyxInspectionService")
public class InspectionServiceImpl extends BaseService {

    @Autowired
    IInspectionDao inspectionDao;

    @Override
    protected void process(String dataSource) throws Exception {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCaches = new HashMap<>();
        while (!isFinish) {
            //总共的records
            List<Inspection> inspectiones = inspectionDao.findInspectionRecord(dataSource, pageNum, getPageSize());
            if (inspectiones != null && inspectiones.size() < getPageSize()) {
                isFinish = true;
            }
            if (inspectiones == null || inspectiones.isEmpty()) {
                continue;
            }
            List<JSONObject> jsonList = new ArrayList<>();
            for (Inspection inspection : inspectiones) {
                Record record = new Record();
                initRecordBasicInfo(record);

                String groupRecordName = inspection.getGroupRecordName();
                //如果cache中已近存在就不在重复查找
                if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
                    List<String> orgOdCategories = inspectionDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
                    orgOdCatCaches.put(groupRecordName, orgOdCategories);
                }
                record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
                //init odCategories
                record.setOdCategories(new String[]{OD_CATEGORY, this.getOdCategory(dataSource)});
                //put MedicalHistory to Record
                putData2Record(inspection, record);
                JSONObject beanJson = bean2Json(record);
                //不需要id
                beanJson.remove("id");
                beanJson.put("_id", new ObjectId().toString());
                jsonList.add(beanJson);
            }
            count += jsonList.size();
            log.info("inserting inspection record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            inspectionDao.batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted inspection records: " + count + " from " + dataSource);
    }

    /**
     * Put MedicalHistory data to Record
     *
     * @param inspection
     * @param record
     */
    private void putData2Record(Inspection inspection, Record record) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        record.setPatientId("shch_" + inspection.getPatientId());
        record.setGroupRecordName(inspection.getGroupRecordName());
        record.setSourceId(inspection.getReportId());
        JSONObject info = record.getInfo();
        //调用提供的方法得到锚点文本
        Map<String, String> stringMap = getFormattedText(inspection);
        info.put(TextFormatter.TEXT, stringMap.get(TextFormatter.TEXT));
        info.put(TextFormatter.TEXT_ARS, stringMap.get(TextFormatter.TEXT_ARS));
    }

    /**
     * Format info.text adn info.textARS
     *
     * @param inspection
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Map<String, String> getFormattedText(Inspection inspection) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<Map<String, String>> infoList = new ArrayList<>();
        for (Inspection.ColumnMapping columnMapping : Inspection.ColumnMapping.values()) {
            Map<String, String> row = new HashMap<>();
            if (!columnMapping.isRequired()) {
                continue;
            }
            row.put(TextFormatter.PROP_NAME, columnMapping.propName());
            row.put(TextFormatter.COLUMN_NAME, columnMapping.columnName());
            infoList.add(row);
        }
        return TextFormatter.textFormatter(infoList, inspection);
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
        record.setRecordType("检查记录");
        record.setSubRecordType("检查");
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
