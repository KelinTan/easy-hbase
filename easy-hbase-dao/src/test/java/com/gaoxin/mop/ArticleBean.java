package com.gaoxin.mop;


import com.gaoxin.mop.annotation.HBaseColumn;

/**
 * 帖子
 *
 * @author Administrator 测试用的Bean
 */
public class ArticleBean {
    private String rowkey;//rowkey


    private String articletp;


    private Integer articleid;      // 帖子id
    @HBaseColumn(family = "d", column = "title", exist = false)
    private String title;           // 帖子标题
    private String summary;         // 帖子描述
    private Byte status;            // 审核状态 -1删除或不过 0审核中 1 审核过
    private Byte kind;              // 奖励制度

    private Long publishtime;       // 发帖时间
    public ArticleBean() {

    }

    public ArticleBean(String rowkey, String articletp, Integer articleid) {
        this.rowkey = rowkey;
        this.articletp = articletp;
        this.articleid = articleid;
    }

    public ArticleBean(String rowkey, String articletp, String title) {
        this.rowkey = rowkey;
        this.articletp = articletp;
        this.title = title;
    }

    public ArticleBean(String rowkey, String articletp, Integer articleid, String title, String summary) {
        this.rowkey = rowkey;
        this.articletp = articletp;
        this.articleid = articleid;
        this.title = title;
        this.summary = summary;
    }

    public Long getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(Long publishtime) {
        this.publishtime = publishtime;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Byte getKind() {
        return kind;
    }

    public void setKind(Byte kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "ArticleBean{" +
                "rowkey='" + rowkey + '\'' +
                ", articletp='" + articletp + '\'' +
                ", articleid=" + articleid +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", status=" + status +
                ", kind=" + kind +
                ", publishtime=" + publishtime +
                '}';
    }
}
