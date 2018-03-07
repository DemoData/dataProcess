package com.example.demo.common.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

@Slf4j
public abstract class GenericDao {
    private RowMapper rowMapper;

    protected <T> List<T> queryForList(JdbcTemplate jdbcTemplate, int currPageNum, int pageSize) throws DataAccessException {
        if (pageSize > 0) {
            int startIndex = (currPageNum - 1) * pageSize;            //开始行索引
            StringBuilder newSql = new StringBuilder(this.generateQuerySql());

            if (startIndex == 0) {
                newSql.append(" limit " + pageSize);
            }
            if (startIndex > 0) {
                newSql.append(" limit ").append(startIndex).append(",").append(pageSize);
            }
            log.info(">>>>>>>>>>sql : " + newSql.toString());
            return jdbcTemplate.query(newSql.toString(), this.generateRowMapper());
        }
        return jdbcTemplate.query(this.generateQuerySql(), this.generateRowMapper());
    }

    protected abstract String generateQuerySql();

    protected abstract <T> RowMapper<T> generateRowMapper();

    public RowMapper getRowMapper() {
        return rowMapper;
    }

    public void setRowMapper(RowMapper assayRowMapper) {
        this.rowMapper = assayRowMapper;
    }
}
