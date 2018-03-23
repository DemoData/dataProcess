package com.example.demo.entity;

import lombok.Data;

@Data
public class Patient {
    private Integer id;

    private String patientId;

    private String sex;

    private String age;

    private String birthDay;//生日

    private String name;

    private String origin;//籍贯

    private String marriage;//婚姻状况

    private String address;//住址

    private Long createTime;

    public enum ColumnMapping {
        ID("_id"),
        BATCH_NO("batchNo"),
        HOSPITAL_ID("hospitalId"),
        SEX("性别"),
        AGE("年龄"),
        BIRTHDAY("出生日期"),
        CREATE_TIME("createTime"),
        ORIGIN("籍贯"),
        MARRIAGE("婚姻状况"),
        ADDRESS("现住址"),
        NAME("姓名");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}
