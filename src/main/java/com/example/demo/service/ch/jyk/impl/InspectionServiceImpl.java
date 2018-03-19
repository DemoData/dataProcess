package com.example.demo.service.ch.jyk.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.ch.IInspectionDao;
import com.example.demo.dao.ch.TextDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Inspection;
import com.example.demo.service.ch.TextService;
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
@Service("chyxInspectionService")
public class InspectionServiceImpl extends TextService<Inspection> {

    @Autowired
    @Qualifier("jyInspectionDao")
    private IInspectionDao inspectionDao;

    @Override
    protected TextDao<Inspection> currentDao() {
        return inspectionDao;
    }

    @Override
    protected void customProcess(Record record, Inspection inspection, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = inspection.getGroupRecordName();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = inspectionDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
    }

    @Override
    protected Map<String, String> getFormattedText(Inspection inspection) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
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

    @Override
    protected void initRecordBasicInfo(Record record, Inspection inspection) {
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
        record.setPatientId("shch_" + inspection.getPatientId());
        record.setGroupRecordName(inspection.getGroupRecordName());
        record.setSourceId(inspection.getReportId());
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
