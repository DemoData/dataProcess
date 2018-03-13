/**
 *
 */
package com.example.demo.common.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;
import java.util.Map;

/**
 * Created by chenzhiheng on 15-3-23.<br>
 * Desc:JDBC基础操作接口实现类
 */
//implements IJdbcOperations
public class JdbcOperations {

    /**
     * 命名语法的JDBC模板
     */
    @Autowired
    private NamedParameterJdbcOperations namedParameterJdbcTemplate;

    /**
     * 占位符语法的JDBC模板
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 默认构造器
     */
    public JdbcOperations() {

    }

    //    @Override
    public int update(String nameSql, Object bean) throws DataAccessException {
        SqlParameterSource sqlParameterSource = this.convertor(bean);
        return this.getNamedParameterJdbcTemplate().update(nameSql, sqlParameterSource);
    }

    //    @Override
    public int update(String nameSql, Map<String, ?> paramMap) throws DataAccessException {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource(paramMap);
        return this.getNamedParameterJdbcTemplate().update(nameSql, sqlParameterSource);
    }

    //    @Override
    public int commonUpdate(String sql, Object... args) throws DataAccessException {
        return this.getJdbcTemplate().update(sql, args);
    }

    //    @Override
    public <T> T selectObject(String nameSql, Map<String, ?> paramMap, Class<T> requiredType) throws DataAccessException {
        SqlParameterSource sqlParameterSource = this.convertor(paramMap);
        RowMapper<T> rowMapper = new BeanPropertyRowMapper<T>(requiredType);
        return this.getNamedParameterJdbcTemplate().queryForObject(nameSql, sqlParameterSource, rowMapper);
    }

    //    @Override
    public <T> T selectObject(String nameSql, Object bean, Class<T> requiredType) throws DataAccessException {
        SqlParameterSource sqlParameterSource = this.convertor(bean);
        RowMapper<T> rowMapper = new BeanPropertyRowMapper<T>(requiredType);
        return this.getNamedParameterJdbcTemplate().queryForObject(nameSql, sqlParameterSource, rowMapper);
    }

    //    @Override
    public Map<String, Object> selectMap(String nameSql, Map<String, ?> paramMap) throws DataAccessException {
        SqlParameterSource sqlParameterSource = this.convertor(paramMap);
        return this.getNamedParameterJdbcTemplate().queryForMap(nameSql, sqlParameterSource);
    }

    //    @Override
    public Map<String, Object> selectMap(String nameSql, Object bean) throws DataAccessException {
        SqlParameterSource sqlParameterSource = this.convertor(bean);
        return this.getNamedParameterJdbcTemplate().queryForMap(nameSql, sqlParameterSource);
    }

    //    @Override
    public List<Map<String, Object>> select(String nameSql, Map<String, ?> paramMap) throws DataAccessException {
        return this.getNamedParameterJdbcTemplate().queryForList(nameSql, paramMap);
    }

    //    @Override
    public List<Map<String, Object>> select(String nameSql, Object bean) throws DataAccessException {
        SqlParameterSource sqlParameterSource = this.convertor(bean);
        return this.getNamedParameterJdbcTemplate().queryForList(nameSql, sqlParameterSource);
    }

    //    @Override
    public <T> List<T> select(String nameSql, Class<T> mappedClass) throws DataAccessException {
        RowMapper<T> rowMapper = new BeanPropertyRowMapper<T>(mappedClass);
        return this.getNamedParameterJdbcTemplate().query(nameSql, rowMapper);
    }

    //    @Override
    public <T> T commonSelectObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
        RowMapper<T> rowMapper = this.convertor(requiredType);
        return this.getJdbcTemplate().queryForObject(sql, rowMapper, args);
    }

    //    @Override
    public Map<String, Object> commonSelectMap(String sql, Object... args) throws DataAccessException {
        return this.getJdbcTemplate().queryForMap(sql, args);
    }

    //    @Override
    public List<Map<String, Object>> commonSelect(String sql, Object... args) throws DataAccessException {
        return this.getJdbcTemplate().queryForList(sql, args);
    }

    //    @Override
    public <T> List<T> commonSelect(String sql, Class<T> mappedClass) throws DataAccessException {
        RowMapper<T> rowMapper = this.convertor(mappedClass);
        return this.getJdbcTemplate().query(sql, rowMapper);
    }

    //    @Override
    public <T> List<T> commonSelect(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return this.getJdbcTemplate().query(sql, rowMapper, args);
    }

    public <T> T selectObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
        return this.getJdbcTemplate().queryForObject(sql, requiredType, args);
    }

    //    @Override
    public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes, int currPageNum, int pageSize) throws DataAccessException {
        if (pageSize > 0) {
            int startIndex = (currPageNum - 1) * pageSize;            //开始行索引
            int endIndex = (currPageNum) * pageSize + 1;              //截止行索引
            StringBuilder newSql = new StringBuilder("SELECT * FROM");
            newSql.append("(SELECT A.*,ROWNUM rn FROM");
            newSql.append(" (" + sql + ") A");
            newSql.append(" where rownum<" + endIndex + ") B");
            newSql.append(" where B.rn>" + startIndex);
            return this.getJdbcTemplate().queryForList(newSql.toString(), args, argTypes);
        }
        return this.getJdbcTemplate().queryForList(sql, args, argTypes);
    }

    /**
     * RowMapper转换器
     *
     * @param mappedClass 待转换的结果类型
     * @param <T>         任意类型
     * @return BeanPropertyRowMapper
     */
    private <T> RowMapper<T> convertor(Class<T> mappedClass) {
        return new BeanPropertyRowMapper(mappedClass);
    }

    /**
     * SqlParameterSource转换器
     *
     * @param bean 待转换的参数值类型
     * @return BeanPropertySqlParameterSource
     */
    private SqlParameterSource convertor(Object bean) {
        return new BeanPropertySqlParameterSource(bean);
    }

    /**
     * SqlParameterSource转换器
     *
     * @param values 待转换的参数值类型
     * @return MapSqlParameterSource
     */
    private SqlParameterSource convertor(Map<String, ?> values) {
        return new MapSqlParameterSource(values);
    }

    /**
     * 获取命名语法JDBC模板
     *
     * @return 命名语法JDBC模板
     */
    public NamedParameterJdbcOperations getNamedParameterJdbcTemplate() {
        return this.namedParameterJdbcTemplate;
    }

    public void setNamedParameterJdbcTemplate(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * 获取占位符语法的JDBC模板
     *
     * @return 占位符语法的JDBC模板
     */
    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
