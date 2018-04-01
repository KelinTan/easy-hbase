package com.gaoxin.mop;


import com.gaoxin.mop.annotation.HBaseColumn;
import com.gaoxin.mop.annotation.RowKey;
import com.gaoxin.mop.constants.HBaseConstant;

/**
 * Demo Bean
 * <p>
 * use @RowKey indicates the rowkey from the hbase
 * use @HBaseColumn indicate the column mapping with the hbase qualifier
 * </p>
 *
 */
public class Article {

    @RowKey
    private String rowkey;

    @HBaseColumn(column = "articletp", family = HBaseConstant.DEFAULT_FAMILY)
    private String articletp ;

    @HBaseColumn(column = "articleid", family = HBaseConstant.DEFAULT_FAMILY)
    private Integer articleid;

    @HBaseColumn(column = "title", family =  HBaseConstant.DEFAULT_FAMILY)
    private String summary;

    @HBaseColumn(column = "status", family = HBaseConstant.DEFAULT_FAMILY)
    private Byte status;
    
    @HBaseColumn(column = "cts", family =  HBaseConstant.DEFAULT_FAMILY)
    private Long cts;               // 发帖时间精确到ms的时间戳

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getArticletp() {
        return articletp;
    }

    public void setArticletp(String articletp) {
        this.articletp = articletp;
    }

    public Integer getArticleid() {
        return articleid;
    }

    public void setArticleid(Integer articleid) {
        this.articleid = articleid;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Long getCts() {
        return cts;
    }

    public void setCts(Long cts) {
        this.cts = cts;
    }

    @Override
    public String toString() {
        return "Article{" +
                "rowkey='" + rowkey + '\'' +
                ", articletp='" + articletp + '\'' +
                ", articleid=" + articleid +
                ", status=" + status +
                '}';
    }
}
