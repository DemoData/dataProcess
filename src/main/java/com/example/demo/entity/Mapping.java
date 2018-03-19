package com.example.demo.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 用于类型映射的处理实体
 */
@Data
public class Mapping implements Comparable<Mapping> {
    private String id;
    private Integer level;
    private List<Mapping> subType;
    private String type;
    private String[] include;
    private String[] exclude;
    private Map<String, String> rule;
    private String mappedValue;
    private Integer rank;

    /**
     * 用于排序处理
     *
     * @param mapping
     * @return
     */
    @Override
    public int compareTo(Mapping mapping) {
        return this.rank - mapping.getRank();
    }
}
