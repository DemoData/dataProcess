package com.example.demo.controller.bdsz.fs;

import com.example.demo.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "北大深圳风湿入库处理控制器")
@RequestMapping("/bdfs")
public class RheumatologistController {

    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("bdfsMedicalHistoryService")
    private IDataService medicalHistoryService;

    @Autowired
    @Qualifier("bdfsAssayService")
    private IDataService assayService;

    @Autowired
    @Qualifier("bdfsInspectionService")
    private IDataService inspectionService;

    @Autowired
    @Qualifier("bdmzMedicalHistoryService")
    private IDataService mzMedicalHistoryService;

    @Autowired
    @Qualifier("bdmzAssayService")
    private IDataService mzAssayService;

    @Autowired
    @Qualifier("bdmzInspectionService")
    private IDataService mzInspectionService;

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

    @GetMapping("/mz/processMedicalHistory")
    public String processMZMedicalHistory() {
        if (mzMedicalHistoryService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/mz/processAssay")
    public String processMZAssay() {
        if (mzAssayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/mz/processInspection")
    public String processMZInspection() {
        if (mzInspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }
}
