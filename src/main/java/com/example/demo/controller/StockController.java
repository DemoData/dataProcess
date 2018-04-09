package com.example.demo.controller;

import com.example.demo.other.BlobToContent;
import com.example.demo.other.DataClean;
import com.example.demo.other.DataToMysql;
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
@Api(tags = "数据入库处理控制器")
@RequestMapping("/stock")
public class StockController {
    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("patientService")
    private IDataService patientService;

    @Autowired
    private DataToMysql dataToMysql;

    @Autowired
    private DataClean dataClean;

    @Autowired
    private BlobToContent blobToContent;

    /**
     * 长海医院Patient数据处理
     *
     * @return
     */
    @GetMapping("/processPatient")
    public String processPatient() {
        if (patientService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

    @GetMapping("/processTest")
    public String processTest() {
        dataToMysql.process();
        return SUCCESS_FLAG;
    }

    @GetMapping("/dataClean")
    public String dataClean() {
        dataClean.updateRecordTypeByAnchor();
        return SUCCESS_FLAG;
    }

    @GetMapping("/blobToContent")
    public String blobToContent() {
        blobToContent.processData();
        return SUCCESS_FLAG;
    }
}
