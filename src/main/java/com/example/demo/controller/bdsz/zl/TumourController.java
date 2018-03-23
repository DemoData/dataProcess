package com.example.demo.controller.bdsz.zl;

import com.example.demo.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author aron
 */
@RestController
@Api(tags = "北大深圳入库肿瘤处理控制器")
@RequestMapping("/bdsz")
public class TumourController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("bdzlMedicalHistoryService")
    private IDataService medicalHistoryService;

    @Autowired
    @Qualifier("bdzlAssayService")
    private IDataService assayService;

    @Autowired
    @Qualifier("bdzlInspectionService")
    private IDataService inspectionService;

    @GetMapping("/processMedicalHistory")
    public String processMedicalHistory() {
        if (medicalHistoryService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processAssay")
    public String processAssay() {
        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processInspection")
    public String processInspection() {
        if (inspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }
}
