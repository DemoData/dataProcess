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
@Api(tags = "长海医院化验数据处理")
@RequestMapping("/chyx")
public class AssayController {

    public static final String SUCCESS_FLAG = "Process Done";
    public static final String FAIL_FLAG = "Process Failure";

    @Autowired
    @Qualifier("chyxAssayService")
    private IDataService assayService;

    @GetMapping("/processAssay")
    public String processAssay() {
        if (assayService.processData()) {
            return SUCCESS_FLAG;
        }
        return FAIL_FLAG;
    }

}
