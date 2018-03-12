package com.example.demo.dao.ch.impl;

import com.example.demo.dao.ch.BaseDao;
import com.example.demo.dao.ch.IMicroorgDao;
import com.example.demo.entity.ch.Microorganism;
import com.example.demo.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class MicroorgDaoImpl extends BaseDao implements IMicroorgDao {

    public List<Record> findAllMicroorgRecord() {
        String sql = "";
//        List<Record> recordList = this.yxzwJdbcTemplate.queryForList(sql, Record.class);
        return null;
    }

    public List<Microorganism> findMicroorgById() {
        String sql = "";

        return null;
    }

    @Override
    protected String generateQuerySql() {
        String sql = "";
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        return null;
    }
}
