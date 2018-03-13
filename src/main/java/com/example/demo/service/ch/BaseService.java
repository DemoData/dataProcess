package com.example.demo.service.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.service.GenericService;
import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.service.IDataService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@PropertySource("classpath:config/service.properties")
public abstract class BaseService extends GenericService implements IDataService {

    @Value("${page.size}")
    private int pageSize;

    @Override
    public boolean processData() {
        try {
            //execute data process in every dataSource
            this.process(MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE);
            this.process(MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE);
            this.process(MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE);
            this.process(MysqlDataSourceConfig.MYSQL_YX_DATASOURCE);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 通过数据源获取OdCategory
     *
     * @param dataSource
     * @return
     */
    protected String getOdCategory(String dataSource) {
        String odCategorie = EMPTY_FLAG;
        if (MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_JKCT;
        }
        if (MysqlDataSourceConfig.MYSQL_YX_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_YX;
        }
        if (MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_YXZW;
        }
        if (MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_TNB;
        }
        return odCategorie;
    }

    /**
     * 处理数据
     *
     * @param dataSource 数据源标识
     */
    protected abstract void process(String dataSource) throws Exception;

    /**
     * 转换为需要入库的json类型
     *
     * @param entity
     * @return
     */
    protected abstract JSONObject bean2Json(Object entity);

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
