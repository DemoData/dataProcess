package com.example.demo.controller.ch.jyk;

import com.example.demo.service.standard.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "长海医院检验科数据处理控制器")
@RequestMapping("/chjy")
public class ClinicalLabContoller {

    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("chyxAssayService")
    private IDataService assayService;

    @Autowired
    @Qualifier("chyxInspectionService")
    private IDataService inspectionService;

    @Autowired
    @Qualifier("chyxMedicalHistoryService")
    private IDataService medicalHistoryService;

    @Autowired
    @Qualifier("chyxMicroorganismService")
    private IDataService microorganismService;

    @Autowired
    @Qualifier("chyxPathologyService")
    private IDataService pathologyService;

    /**
     * 长海医院化验数据处理
     *
     * @return
     */
    @GetMapping("/processAssay")
    public String processAssay() {
        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    /**
     * 长海医院检查数据处理
     *
     * @return
     */
    @GetMapping("/processInspection")
    public String processInspection() {
        if (inspectionService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    /**
     * 长海医院病历文本数据处理
     *
     * @return
     */
    @GetMapping("/processMedicalHistory")
    public String processMedicalHistory() {
        if (medicalHistoryService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }


    /**
     * 长海医院微生物数据处理
     *
     * @return
     */
    @GetMapping("/processMicroorganism")
    public String processMicroorganism() {
        if (microorganismService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }


    /**
     * 长海医院病理数据处理
     *
     * @return
     */
    @GetMapping("/processPathology")
    public String processPathology() {
        if (pathologyService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}
