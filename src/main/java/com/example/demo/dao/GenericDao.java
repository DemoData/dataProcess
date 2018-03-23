package com.example.demo.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

@Slf4j
public abstract class GenericDao {
    private RowMapper rowMapper;

    /**
     * mysql query with page
     *
     * @param jdbcTemplate
     * @param currPageNum
     * @param pageSize
     * @param <T>
     * @return
     * @throws DataAccessException
     */
    protected <T> List<T> queryForList(JdbcTemplate jdbcTemplate, int currPageNum, int pageSize) throws DataAccessException {
        if (pageSize > 0) {
            int startIndex = (currPageNum - 1) * pageSize;            //开始行索引
            StringBuilder newSql = new StringBuilder(this.generateQuerySql());
            if (StringUtils.isBlank(this.generateQuerySql())) {
                log.error("queryForList(): sql is empty");
                return null;
            }
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

    /**
     * 针对sqlserver的分页
     *
     * @param jdbcTemplate
     * @param currPageNum
     * @param pageSize
     * @param <T>
     * @return
     * @throws DataAccessException
     */
    protected <T> List<T> queryForListInSqlServer(JdbcTemplate jdbcTemplate, int currPageNum, int pageSize, String tableName, String displayColumns, String conditions) throws DataAccessException {
        if (pageSize > 0) {
            int startIndex = (currPageNum - 1) * pageSize;
            int endIndex = startIndex + pageSize + 1;
            StringBuilder pagedSql = new StringBuilder("select * from (select row_number()over(order by id)rownumber,");
            if (StringUtils.isNotEmpty(displayColumns)) {
                pagedSql.append(displayColumns);
            } else {
                pagedSql.append("*");
            }
            pagedSql.append(" from ").append(tableName).append(" ");
            if (StringUtils.isNotEmpty(conditions)) {
                pagedSql.append(conditions);
            }
            pagedSql.append(") t").append(" where rownumber>").append(startIndex).append(" and ").append("rownumber<").append(endIndex);
            log.info(">>>>>>>>>>sql : " + pagedSql.toString());
            return jdbcTemplate.query(pagedSql.toString(), this.generateRowMapper());
        }
        return jdbcTemplate.query(this.generateQuerySql(), this.generateRowMapper());
    }

    /**
     * This sql use for method queryForList()
     *
     * @return
     */
    protected abstract String generateQuerySql();

    protected abstract <T> RowMapper<T> generateRowMapper();

    public RowMapper getRowMapper() {
        return rowMapper;
    }

    public void setRowMapper(RowMapper assayRowMapper) {
        this.rowMapper = assayRowMapper;
    }
}
