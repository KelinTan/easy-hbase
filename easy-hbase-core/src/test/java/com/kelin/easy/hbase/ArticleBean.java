package com.kelin.easy.hbase;


/**
 * 帖子
 *
 * @author Kelin Tan
 */
public class ArticleBean {
    public ArticleBean() {
    }

    public ArticleBean(String rowkey, int type, String title, Long status, Boolean pubilshed) {
        this.rowkey = rowkey;
        this.type = type;
        this.title = title;
        this.status = status;
        this.pubilshed = pubilshed;
    }

    private String rowkey;//rowkey
    private Integer type;
    private String title;
    private Long status;
    private Boolean pubilshed;

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Boolean getPubilshed() {
        return pubilshed;
    }

    public void setPubilshed(Boolean pubilshed) {
        this.pubilshed = pubilshed;
    }
}
