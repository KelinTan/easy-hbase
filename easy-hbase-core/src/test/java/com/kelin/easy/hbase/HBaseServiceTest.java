package com.kelin.easy.hbase;

import com.google.common.collect.Lists;
import com.kelin.easy.hbase.bean.ColumnInfo;
import com.kelin.easy.hbase.constants.HBaseConstant;
import com.kelin.easy.hbase.core.FakeHBaseConnectionService;
import com.kelin.easy.hbase.core.HBaseTestingUtilityManager;
import com.kelin.easy.hbase.core.HBaseService;
import com.kelin.easy.hbase.core.HBaseServiceImpl;
import org.apache.hadoop.hbase.CompareOperator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kelin Tan
 */
public class HBaseServiceTest {

    private static HBaseService service;

    private static final String DEFAULT_TABLE = "article";

    @BeforeClass
    public static void setUp() {
        service = new HBaseServiceImpl(new FakeHBaseConnectionService());

        //init data
        List<ArticleBean> articleBeans = new ArrayList<>();
        articleBeans.add(new ArticleBean("123", 1, "测试123", 2L, true));
        articleBeans.add(new ArticleBean("124", 2, "测试", 2L, true));
        articleBeans.add(new ArticleBean("135", 3, "测试", 2L, true));

        HBaseTestingUtilityManager.createTable(DEFAULT_TABLE, HBaseConstant.DEFAULT_FAMILY);

        service.put(DEFAULT_TABLE, articleBeans);
    }

    @Test
    public void testGetRowKeys() {
        List<String> rowKeys = service.getRowKeys(DEFAULT_TABLE);

        Assert.assertEquals(3, rowKeys.size());
        Assert.assertEquals("123", rowKeys.get(0));
        Assert.assertEquals("124", rowKeys.get(1));
    }

    @Test
    public void testRowKeysPage() {
        List<String> rowKeys = service.getRowKeys(DEFAULT_TABLE, "123", "124", 1);
        Assert.assertEquals(1, rowKeys.size());
        Assert.assertEquals("123", rowKeys.get(0));
    }

    @Test
    public void testGetList() {
        List<ArticleBean> list = service.getList(DEFAULT_TABLE, Lists.newArrayList("123", "124"), ArticleBean.class);

        Assert.assertEquals(2, list.size());
        Assert.assertEquals(1, (int) list.get(0).getType());
        Assert.assertEquals("测试123", list.get(0).getTitle());
        Assert.assertEquals(2L, (long) list.get(0).getStatus());
        Assert.assertTrue(list.get(0).getPubilshed());
    }

    @Test
    public void testGetListFilter() {
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setColumn("title");
        columnInfo.setValue("测试");
        columnInfo.setCompareOperator(CompareOperator.NOT_EQUAL);
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        columnInfoList.add(columnInfo);

        List<ArticleBean> list = service.getList(DEFAULT_TABLE, Lists.newArrayList("123", "124"), null, columnInfoList,
                ArticleBean.class);

        Assert.assertEquals(1, list.size());
        Assert.assertEquals("测试123", list.get(0).getTitle());
    }

    @Test
    public void testFilterLongValue() {
        List<ColumnInfo> filters = new ArrayList<>();
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setColumn("type");
        columnInfo.setValue("2");
        columnInfo.setCompareOperator(CompareOperator.EQUAL);
        //如果是long类型的值过滤 需添加class标识，否则默认为string的字节数组过滤
        columnInfo.setValueClass(Integer.class);
        filters.add(columnInfo);

        ArticleBean bean = service.get(DEFAULT_TABLE, "123", null, filters, ArticleBean.class);
        Assert.assertNull(bean);

        Assert.assertNotNull(service.get(DEFAULT_TABLE, "123", null, null, ArticleBean.class));
    }
}
