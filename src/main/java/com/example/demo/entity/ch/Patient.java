package com.example.demo.entity.ch;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
public class Patient {
    private String id;

    private String patientId;

    private String sex;

    private String age;

    private String clinicDate;

    private String birthDay;

    private String name;

    public enum ColumnMapping {
        ID("_id"),
        BATCH_NO("batchNo"),
        SOURCE("source"),
        HOSPITAL_ID("hospitalId"),
        SEX("性别"),
        AGE("年龄"),
        BIRTHDAY("出生年"),
        NAME("name");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}
