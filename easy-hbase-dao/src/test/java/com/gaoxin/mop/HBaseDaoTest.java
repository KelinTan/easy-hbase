package com.gaoxin.mop;

import com.gaoxin.mop.bean.ColumnInfo;
import com.gaoxin.mop.config.HBaseFactoryBean;
import com.gaoxin.mop.dao.HBaseDao;
import com.google.common.collect.Lists;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Mr.tan
 * Date:  2018/3/20
 * Description: HBase 相关方法测试用例
 */
public class HBaseDaoTest extends BaseSpringTest {

    @Autowired
    private HBaseDao hBaseDao;

    @Test
    public void testGetRowKeys() {

        System.out.println(hBaseDao.getRowKeys("mopnovel_user_comment"));
    }

    @Test
    public void testRowKeysPage() {
        System.out.println(hBaseDao.getRowKeys("mop_user_feed_desc", "321547154", "321547155", 10, "_"));
    }

    @Test
    public void testGetList() {
        System.out.println(hBaseDao.getList("mop_articles_desc", Lists.newArrayList("7703046885167257003", "7703046885167257003"), ArticleBean.class));
    }

    @Test
    public void testGetListFilter() {
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setColumn("status");
        columnInfo.setValue("-1");
        columnInfo.setCompareOperator(CompareFilter.CompareOp.NOT_EQUAL);
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        columnInfoList.add(columnInfo);
        System.out.println(hBaseDao.getList("mop_articles_desc", Lists.newArrayList("7703046885167257003"), null, columnInfoList, ArticleBean.class));
    }

    @Test
    public void testNoAnnotationGet() {

        System.out.println(hBaseDao.get("mop_articles_desc", "7703046885167257003", ArticleBean.class));
    }

    @Test
    public void testNoAnnotationPut() {

        List<ArticleBean> articleBeans = new ArrayList<>();
        articleBeans.add(new ArticleBean("11", "dzh", "测试忽略"));
        articleBeans.add(new ArticleBean("22", "dzh", 23, "忽略标题", "不忽略中文"));
        System.out.println(hBaseDao.put("mop_articles_desc", articleBeans));
    }

    @Test
    public void testHBaseRowKeyPrefix() throws IOException {

        HTable hTable = (HTable) HBaseFactoryBean.getDefaultConnection().getTable("mop_user_feed_asc");
        Scan scan = new Scan();
        scan.setStartRow("0750001".getBytes());
        scan.setStopRow("0750002".getBytes());
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        Filter kof = new KeyOnlyFilter();
        filterList.addFilter(kof);
        scan.setFilter(filterList);

        ResultScanner scanner = hTable.getScanner(scan);

        for (Result result : scanner) {
            // 判断结果是否为空,是的话则跳过
            if (!result.isEmpty()) {
                System.out.println(new String(result.getRow()));
            }
        }
    }


    @Test
    public void testFilterLongValue() {

        List<ColumnInfo> filters = new ArrayList<>();
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setColumn("publishtime");
        columnInfo.setValue("1521429162015");
        columnInfo.setCompareOperator(CompareFilter.CompareOp.NOT_EQUAL);
        //如果是long类型的值过滤 需添加class标识，否则默认为string的字节数组过滤
        columnInfo.setValueClass(Long.class);
        filters.add(columnInfo);
        System.out.println(hBaseDao.get("mop_articles_desc", "7701942874838942496", null, filters, ArticleBean.class));


    }

}
