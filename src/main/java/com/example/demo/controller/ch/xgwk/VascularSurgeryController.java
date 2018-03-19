package com.example.demo.controller.ch.xgwk;

import com.example.demo.service.IDataService;
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
@Api(tags = "长海医院血管外科数据处理控制器")
@RequestMapping("/chxg")
public class VascularSurgeryController {

    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("chxgMedicalOrderService")
    private IDataService medicalOrderService;

    @Autowired
    @Qualifier("chxgAssayService")
    private IDataService assayService;

    @Autowired
    @Qualifier("chxgInspectionFSService")
    private IDataService inspectionFSService;

    @Autowired
    @Qualifier("chxgInspectionCSXDService")
    private IDataService inspectionCSXDService;

    @GetMapping("/processMedicalOrder")
    public String processMedicalOrder() {
        if (medicalOrderService.processData()) {
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

    @GetMapping("/processInspectionFS")
    public String processInspectionFS() {
        if (inspectionFSService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processInspectionCSXD")
    public String processInspectionCSXD() {
        if (inspectionCSXDService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }
}

