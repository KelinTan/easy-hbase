package com.kelin.easy.hbase;

import com.google.common.collect.Lists;
import com.kelin.easy.hbase.bean.ColumnInfo;
import com.kelin.easy.hbase.constants.HBaseConstant;
import com.kelin.easy.hbase.core.FakeHBaseConnectionService;
import com.kelin.easy.hbase.core.HBaseService;
import com.kelin.easy.hbase.core.HBaseServiceImpl;
import com.kelin.easy.hbase.core.HBaseTestingUtilityManager;
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

    private static final String DEMO_TABLE = "demo";

    @BeforeClass
    public static void setUp() {
        service = new HBaseServiceImpl(new FakeHBaseConnectionService());

        //init table
        HBaseTestingUtilityManager.createTable(DEFAULT_TABLE, HBaseConstant.DEFAULT_FAMILY);
        HBaseTestingUtilityManager.createTable(DEMO_TABLE, HBaseConstant.DEFAULT_FAMILY);

        //init data
        List<ArticleBean> articleBeans = new ArrayList<>();
        articleBeans.add(new ArticleBean("123", 1, "测试123", 2L, true));
        articleBeans.add(new ArticleBean("124", 2, "测试", 2L, true));
        articleBeans.add(new ArticleBean("135", 3, "测试", 2L, true));
        service.put(DEFAULT_TABLE, articleBeans);
        service.put(DEMO_TABLE, "test", "1", "2");
    }

    @Test
    public void testGet() {
        List<ColumnInfo> list = new ArrayList<>();
        list.add(new ColumnInfo("title"));
        ArticleBean articleBean = service.get(DEFAULT_TABLE, "123", list, ArticleBean.class);
        Assert.assertNotNull(articleBean);
        Assert.assertNull(articleBean.getStatus());
        Assert.assertEquals("测试123", articleBean.getTitle());
    }

    @Test
    public void testGetSingleColumnValue() {
        String title = service.getSingleColumnValue(DEFAULT_TABLE, "123", "title");
        Assert.assertEquals("测试123", title);
    }

    @Test
    public void testGetFilterLongValue() {
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

    @Test
    public void testGetColumns() {
        List<ColumnInfo> columns = service.getColumns(DEFAULT_TABLE, "123");
        Assert.assertEquals(4, columns.size());
        Assert.assertEquals("published", columns.get(0).getColumn());
        Assert.assertEquals("true", columns.get(0).getValue());
    }

    @Test
    public void testGetRowKeys() {
        List<String> rowKeys = service.getRowKeys(DEFAULT_TABLE);

        Assert.assertEquals(3, rowKeys.size());
        Assert.assertEquals("123", rowKeys.get(0));
        Assert.assertEquals("124", rowKeys.get(1));
    }

    @Test
    public void testGetRowKeysByPrefix() {
        List<String> rowKeys = service.getRowKeysByPrefix(DEFAULT_TABLE, "13");

        Assert.assertEquals(1, rowKeys.size());
        Assert.assertEquals("135", rowKeys.get(0));
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
        Assert.assertTrue(list.get(0).getPublished());
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
    public void testGetPageList() {
        List<ArticleBean> list = service.getPageList(DEFAULT_TABLE, "123", "124", 1, ArticleBean.class);
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testDelete() {
        Assert.assertNotNull(service.getSingleColumnValue(DEMO_TABLE, "test", "1"));
        service.delete(DEMO_TABLE, "test");
        Assert.assertNull(service.getSingleColumnValue(DEMO_TABLE, "test", "1"));
    }
}
