package com.example.demo.dao.standard;

import com.example.demo.dao.TextDao;
import com.example.demo.entity.Pathology;

import java.util.List;

/**
 * @author aron
 */
public interface IPathologyDao extends TextDao<Pathology> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

}
