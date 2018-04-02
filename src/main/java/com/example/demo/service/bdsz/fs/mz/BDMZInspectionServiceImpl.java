package com.example.demo.service.bdsz.fs.mz;

import com.example.demo.common.util.TimeUtil;
import com.example.demo.entity.Inspection;
import com.example.demo.entity.Record;
import com.example.demo.service.bdsz.fs.BDFSInspectionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdmzInspectionService")
public class BDMZInspectionServiceImpl extends BDFSInspectionServiceImpl {

    private Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    @Override
    protected void initRecordBasicInfo(Record record, Inspection inspection) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180328");
        record.setDepartment("风湿免疫科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("门诊-检查");
        record.setStatus("AMD识别完成");
        record.setRecordType("检查记录");
        record.setSubRecordType("检查");
        //
        record.setPatientId("bdsz_" + inspection.getPatientId());
        record.setSourceId(inspection.getHospitalId());
        record.setGroupRecordName(inspection.getHospitalId());
        record.setCreateTime(currentTimeMillis);
    }

    @Override
    protected void customProcess(Record record, Inspection entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        record.setOdCategories(new String[]{"风湿"});
    }
}
