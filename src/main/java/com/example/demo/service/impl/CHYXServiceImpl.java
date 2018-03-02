package com.example.demo.service.impl;

import com.example.demo.dao.ICHYXDao;
import com.example.demo.service.ICHYXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Service
public class CHYXServiceImpl implements ICHYXService {

    @Autowired
    ICHYXDao chyxDao;

    @Override
    public boolean processPancreasData() {
        //TODO:
        if (log.isInfoEnabled()) {
            log.info("===enter processPancreasData===");
        }
        chyxDao.processTest();
        return true;
    }
}
