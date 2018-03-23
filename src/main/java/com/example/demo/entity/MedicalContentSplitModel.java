package com.example.demo.entity;

import lombok.Data;

/**
 * 病历内容拆分更新实体类
 */
@Data
public class MedicalContentSplitModel {
    private String visitNumber;
    private String createDate;
    private String medicalContent;
    private Integer status;
}
