package com.example.demo.service.bdsz.fs;

import com.example.demo.common.constant.CommonConstant;
import com.example.demo.entity.Record;
import com.example.demo.service.bdsz.zl.BDZLAssayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdfsAssayService")
public class BDFSAssayServiceImpl extends BDZLAssayServiceImpl {

    @Override
    protected void initRecordBasicInfo(Record record) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180328");
        record.setDepartment("风湿免疫科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("化验");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("化验");
        record.setPatientId(StringUtils.isEmpty(record.getPatientId()) ? CommonConstant.EMPTY_FLAG : "bdsz_" + record.getPatientId());
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        super.customProcess(record, orgOdCatCaches, patientCaches, dataSource);
        record.setOdCategories(new String[]{"风湿"});
    }
}
