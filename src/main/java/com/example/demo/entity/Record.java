package com.example.demo.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Record {
    @Id
    private String id;
    private String hospitalId;
    private String userId;
    private String groupRecordName;//一次就诊号
    private String patientId;
    private String templateId;
    private String department;
    private JSONObject info = new JSONObject();
    private String recordType;
    private String subRecordType;
    private String[] odCategories;
    private String[] orgOdCategories;
    private String sourceId;
    private String format;

    public enum MongoColumn {
        ID("_id");

        private final String value;

        MongoColumn(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}
