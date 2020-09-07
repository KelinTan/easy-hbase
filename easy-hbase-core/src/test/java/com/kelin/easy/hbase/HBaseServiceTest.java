package com.kelin.easy.hbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import com.google.common.collect.Lists;
import com.kelin.easy.hbase.common.bean.ColumnInfo;
import com.kelin.easy.hbase.common.constants.HBaseConstant;
import com.kelin.easy.hbase.core.HBaseService;
import com.kelin.easy.hbase.core.HBaseServiceImpl;
import com.kelin.easy.hbase.systest.FakeHBaseConnectionService;
import com.kelin.easy.hbase.systest.HBaseTestingUtilityManager;
import org.apache.hadoop.hbase.CompareOperator;
import org.junit.AfterClass;
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

    @AfterClass
    public static void shutdown() throws Exception {
        HBaseTestingUtilityManager.getInstance().shutdownMiniCluster();
    }

    @Test
    public void testGet() {
        List<ColumnInfo> list = new ArrayList<>();
        list.add(new ColumnInfo("title"));
        ArticleBean articleBean = service.get(DEFAULT_TABLE, "123", list, ArticleBean.class);

        assertThat(articleBean).isNotNull();
        assertThat(articleBean.getStatus()).isNull();
        assertThat(articleBean.getTitle()).isEqualTo("测试123");
    }

    @Test
    public void testGetSingleColumnValue() {
        assertThat(service.getSingleColumnValue(DEFAULT_TABLE, "123", "title")).isEqualTo("测试123");
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

        assertThat(service.get(DEFAULT_TABLE, "123", null, filters, ArticleBean.class)).isNull();
        assertThat(service.get(DEFAULT_TABLE, "123", null, null, ArticleBean.class)).isNotNull();
    }

    @Test
    public void testGetColumns() {
        assertThat(service.getColumns(DEFAULT_TABLE, "123"))
                .hasSize(4)
                .first()
                .isEqualToIgnoringNullFields(new ColumnInfo(null, "published", "true", null));
    }

    @Test
    public void testGetRowKeys() {
        assertThat(service.getRowKeys(DEFAULT_TABLE))
                .hasSize(3)
                .contains("123", atIndex(0))
                .contains("124", atIndex(1));
    }

    @Test
    public void testGetRowKeysByPrefix() {
        assertThat(service.getRowKeysByPrefix(DEFAULT_TABLE, "13"))
                .hasSize(1)
                .contains("135", atIndex(0));
    }

    @Test
    public void testRowKeysPage() {
        assertThat(service.getRowKeys(DEFAULT_TABLE, "123", "124", 1))
                .hasSize(1)
                .contains("123", atIndex(0));
    }

    @Test
    public void testGetList() {
        assertThat(service.getList(DEFAULT_TABLE, Lists.newArrayList("123", "124"), ArticleBean.class))
                .hasSize(2)
                .first()
                .isEqualToIgnoringNullFields(new ArticleBean("123", 1, "测试123", 2L, true));
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
