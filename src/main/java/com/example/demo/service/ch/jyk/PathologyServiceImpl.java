package com.example.demo.service.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.standard.IPathologyDao;
import com.example.demo.dao.TextDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.Pathology;
import com.example.demo.service.TextService;
import lombok.extern.slf4j.Slf4j;
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
@Service("chyxPathologyService")
public class PathologyServiceImpl extends TextService<Pathology> {

    @Autowired
    IPathologyDao pathologyDao;

    @Override
    protected TextDao<Pathology> currentDao() {
        return pathologyDao;
    }

    @Override
    protected void customProcess(Record record, Pathology pathology, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = pathology.getGroupRecordName();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = pathologyDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        //init odCategories
        record.setOdCategories(new String[]{OD_CATEGORY, this.getOdCategory(dataSource)});
    }

    @Override
    protected Map<String, String> getFormattedText(Pathology pathology) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<Map<String, String>> infoList = new ArrayList<>();
        for (Pathology.ColumnMapping pathologyEnum : Pathology.ColumnMapping.values()) {
            Map<String, String> row = new HashMap<>();
            if (!pathologyEnum.isRequired()) {
                continue;
            }
            row.put(TextFormatter.PROP_NAME, pathologyEnum.propName());
            row.put(TextFormatter.COLUMN_NAME, pathologyEnum.columnName());
            infoList.add(row);
        }
        return TextFormatter.textFormatter(infoList, pathology);
    }

    @Override
    protected void initRecordBasicInfo(Record record, Pathology pathology) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setUserId("5a7c0adcc2f9c4944dd2b070");
        record.setBatchNo("shch20180309");
        record.setTemplateId(EMPTY_FLAG);
        record.setDepartment("检验科");
        record.setRecordType("病理");
        record.setSubRecordType("病理");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setPatientId("shch_" + pathology.getPatientId());
        record.setGroupRecordName(pathology.getGroupRecordName());
        record.setSourceId(pathology.getId().toString());
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
