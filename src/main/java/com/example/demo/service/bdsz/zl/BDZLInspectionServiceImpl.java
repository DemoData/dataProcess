package com.example.demo.service.bdsz.zl;

import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.TextDao;
import com.example.demo.dao.standard.IInspectionDao;
import com.example.demo.entity.Inspection;
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
@Service("bdzlInspectionService")
public class BDZLInspectionServiceImpl extends TextService<Inspection> {

    @Autowired
    @Qualifier("bdzlInspectionDao")
    private IInspectionDao inspectionDao;

    @Override
    protected TextDao<Inspection> currentDao() {
        return inspectionDao;
    }

    @Override
    protected void customProcess(Record record, Inspection entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = inspectionDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }

        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setOdCategories(new String[]{"肿瘤"});
    }

    @Override
    protected Map<String, String> getFormattedText(Inspection entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
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
        return TextFormatter.textFormatter(infoList, entity);
    }

    @Override
    protected void initRecordBasicInfo(Record record, Inspection inspection) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180320");
        record.setDepartment("肿瘤内科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("检查");
        record.setStatus("AMD识别完成");
        record.setRecordType("检查记录");
        record.setSubRecordType("检查");
        //
        record.setPatientId("bdsz_" + inspection.getPatientId());
        record.setSourceId(inspection.getHospitalId());
        record.setGroupRecordName(inspection.getHospitalId());
    }


}