package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.IMedicalContentSplitService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liulun
 * 病历相同内容拆分
 */

@Api("病历文书中存在同一个就诊号多条记录，但病历内容合并的情况，此类就是拆分合并的病历内容并更新就诊号对应的病历内容")
@RestController
public class MedicalContentSplitController {

    @Autowired
    private IMedicalContentSplitService iMedicalContentSplitService;


    @GetMapping("/medicalContentSplit")
    public boolean medicalContentSplit(@RequestParam String type){
        return iMedicalContentSplitService.medicalContentSplit(type);
    }

    @GetMapping("/datacul")
    public Integer datacul(@RequestParam String sql){
        return iMedicalContentSplitService.datacul(sql).size();
    }

    @GetMapping("/dataculAdd")
    public Integer dataculAdd(@RequestParam String sql){
        return iMedicalContentSplitService.dataculAdd(sql);
    }


    @GetMapping("importExcel")
    public boolean importExcel(){
        return iMedicalContentSplitService.importYXXGExcel();
    }

    @GetMapping("pandian")
    public JSONObject pandian(){
        return iMedicalContentSplitService.pandian();
    }

    @GetMapping("mongoPandian")
    public JSONObject mongoPandian(@RequestParam String batchNo){
        return iMedicalContentSplitService.mongoPandian(batchNo);
    }
}
