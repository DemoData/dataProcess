package com.example.demo.entity;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Record {
    private String id;
    private String hospitalId;
    private String userId;
    private String groupRecordName;//一次就诊号
    private String patientId;
    private String batchNo;
    private String templateId;
    private String department;
    private JSONObject info = new JSONObject();
    private String recordType;
    private String subRecordType;
    private String sourceRecordType;
    private String[] odCategories;
    private String[] orgOdCategories;
    private String sourceId;
    private String format;
    private boolean deleted;
    private String source;
    private String status;
    private Long createTime;

    public Record() {
        initial();
    }

    private void initial() {
        //init info
        List<Map<String, String>> detailArray = new ArrayList<>();
        List<Map<String, String>> formattedText = new ArrayList<>();
        List<Map<String, String>> table = new ArrayList<>();
        this.info.put("basicInfo", new JSONObject());
        this.info.put("detailArray", detailArray);
        this.info.put("text", CommonConstant.EMPTY_FLAG);
        this.info.put("textARS", CommonConstant.EMPTY_FLAG);
        this.info.put("formattedText", formattedText);
        this.info.put("table", table);
        this.hospitalId = CommonConstant.EMPTY_FLAG;
        this.userId = CommonConstant.EMPTY_FLAG;
        this.groupRecordName = CommonConstant.EMPTY_FLAG;
        this.patientId = CommonConstant.EMPTY_FLAG;
        this.batchNo = CommonConstant.EMPTY_FLAG;
        this.templateId = CommonConstant.EMPTY_FLAG;
        this.department = CommonConstant.EMPTY_FLAG;
        this.recordType = CommonConstant.EMPTY_FLAG;
        this.subRecordType = CommonConstant.EMPTY_FLAG;
        this.sourceId = CommonConstant.EMPTY_FLAG;
        this.format = CommonConstant.EMPTY_FLAG;
        this.source = CommonConstant.EMPTY_FLAG;
        this.status = CommonConstant.EMPTY_FLAG;
        this.sourceRecordType = CommonConstant.EMPTY_FLAG;
    }
}
