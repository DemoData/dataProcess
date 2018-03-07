package com.example.demo.controller;

import com.example.demo.service.IMedicalContentSplitService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    public boolean medicalContentSplit(){
        return iMedicalContentSplitService.medicalContentSplit();
    }

}
