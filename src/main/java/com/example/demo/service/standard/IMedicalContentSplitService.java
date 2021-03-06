package com.example.demo.service.standard;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Set;

public interface IMedicalContentSplitService {

    boolean medicalContentSplit(String type);

    Set<String> datacul(String sql);

    Integer dataculAdd(String sql);

    boolean importYXXGExcel();

    JSONObject pandian();

    JSONObject mongoPandian(String batchNo);

    void menzhenMongoPandian(String batchNo);
}
