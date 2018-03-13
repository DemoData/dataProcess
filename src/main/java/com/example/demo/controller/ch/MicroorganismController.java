package com.example.demo.controller.ch;

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
@Api(tags = "长海医院微生物数据处理")
@RequestMapping("/chyx")
public class MicroorganismController {

    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("chyxMicroorganismService")
    private IDataService microorganismService;

    @GetMapping("/processMicroorganism")
    public String processMedicalHistory() {
        if (microorganismService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}
