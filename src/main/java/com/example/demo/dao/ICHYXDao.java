package com.example.demo.dao;

import com.example.model.MedicalContentSplitModel;

import java.util.List;
import java.util.Map;

public interface ICHYXDao {

    void processTest();

    List<Map<String, Object>> findMedicalContentCountMap();

    List<Map<String, Object>> findCreateDateMedicalNameMapByVisitNumberAndMedicalContent(String visitNumber, String medicalContent);

    Map<String, Object> findByVisitNumberAndMedicalContentLimitOne(String visitNumber, String medicalContent);

    int update(MedicalContentSplitModel medicalContentSplitModel);

    int forbid(MedicalContentSplitModel medicalContentSplitModel);

    void add(Map<String, Object> map);
}
