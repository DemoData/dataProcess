package com.example.demo.dao;

import java.util.List;

public interface PandianDao {

    List<String> findIdByBatchNo(String batchNo);
}
