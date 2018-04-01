package com.gaoxin.mop.bean;

import java.util.List;

/**
 * Created by cch
 * 2018-03-21 18:59.
 */

public class Page<T> {
    private Integer pageSize;

    private Integer pageNo;

    //当前list数量
    private int counts;

    private List<T> entityList;

    //总数
    private long totalCounts;


    public long getTotalCounts() {
        return totalCounts;
    }

    public void setTotalCounts(long totalCounts) {
        this.totalCounts = totalCounts;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    public List<T> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<T> entityList) {
        this.entityList = entityList;
    }

}
