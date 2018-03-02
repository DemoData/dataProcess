package com.example.demo.controller;

import com.example.demo.service.ICHYXService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author aron
 * @date 2018.02.27
 */
@RestController
@Api(tags = "Patient数据处理")
public class CHYXProcessController {

    @Autowired
    public ICHYXService chyxService;

    @GetMapping("/processChyxData")
    public boolean processChyxData() {
        //TODO:
        chyxService.processPancreasData();
        return false;
    }

}
