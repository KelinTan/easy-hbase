// Copyright 2020 Kelin Inc. All rights reserved.

package com.kelin.easy.hbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import com.google.common.collect.Lists;
import com.kelin.easy.hbase.common.bean.ColumnInfo;
import com.kelin.easy.hbase.common.constants.HBaseConstant;
import com.kelin.easy.hbase.common.json.JsonConverter;
import com.kelin.easy.hbase.core.HBaseService;
import com.kelin.easy.hbase.core.HBaseServiceImpl;
import com.kelin.easy.hbase.systest.FakeHBaseConnectionService;
import com.kelin.easy.hbase.systest.HBaseTestingUtilityManager;
import org.apache.hadoop.hbase.CompareOperator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kelin Tan
 */
public class HBaseServiceTest {
    private static HBaseService service;

    private static final String BEAN_TABLE = "bean";

    private static final String COLUMN_TABLE = "column";

    private static final String COLUMN_OBJ_TABLE = "column_obj";

    @BeforeClass
    public static void setUp() {
        service = new HBaseServiceImpl(new FakeHBaseConnectionService());

        //init table
        HBaseTestingUtilityManager.createTable(BEAN_TABLE, HBaseConstant.DEFAULT_FAMILY);
        HBaseTestingUtilityManager.createTable(COLUMN_TABLE, HBaseConstant.DEFAULT_FAMILY);
        HBaseTestingUtilityManager.createTable(COLUMN_OBJ_TABLE);

        //init data
        List<ArticleBean> articleBeans = new ArrayList<>();
        articleBeans.add(new ArticleBean("123", 1, "测试123", 2L, true));
        articleBeans.add(new ArticleBean("124", 2, "测试", 2L, true));
        articleBeans.add(new ArticleBean("135", 3, "测试", 2L, true));
        service.put(BEAN_TABLE, articleBeans);

        service.put(COLUMN_TABLE, "test", "1", "1");
        service.put(COLUMN_TABLE, "test", "2", "2");
        service.put(COLUMN_TABLE, "test2", "2", "2");
        service.put(COLUMN_TABLE, "test3", "1", "1");
        service.put(COLUMN_TABLE, "test3", "2", "2");

        service.put(COLUMN_OBJ_TABLE, "test", "a",
                JsonConverter.serialize(new ArticleBean("a", 1, "测试a", 2L, true)));
        service.put(COLUMN_OBJ_TABLE, "test", "b",
                JsonConverter.serialize(new ArticleBean("b", 1, "测试b", 2L, true)));
    }

    @AfterClass
    public static void shutdown() throws Exception {
        HBaseTestingUtilityManager.getInstance().shutdownMiniCluster();
    }

    @Test
    public void testGet() {
        List<ColumnInfo> list = new ArrayList<>();
        list.add(new ColumnInfo("title"));
        ArticleBean articleBean = service.get(BEAN_TABLE, "123", list, ArticleBean.class);

        assertThat(articleBean).isNotNull();
        assertThat(articleBean.getStatus()).isNull();
        assertThat(articleBean.getTitle()).isEqualTo("测试123");
    }

    @Test
    public void testGetSingleColumnValue() {
        assertThat(service.getSingleColumnValue(BEAN_TABLE, "123", "title")).isEqualTo("测试123");
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

        assertThat(service.get(BEAN_TABLE, "123", null, filters, ArticleBean.class)).isNull();
        assertThat(service.get(BEAN_TABLE, "123", null, null, ArticleBean.class)).isNotNull();
    }

    @Test
    public void testGetColumnsWithFilter() {
        List<ColumnInfo> columns = service.getColumns(BEAN_TABLE, "123", Lists.newArrayList(new ColumnInfo("title")),
                Lists.newArrayList(new ColumnInfo("title", "测试123")));
        assertThat(columns).hasSize(1);
        assertThat(columns.get(0).getColumn()).isEqualTo("title");
        assertThat(columns.get(0).getValue()).isEqualTo("测试123");
    }

    @Test
    public void testGetColumnsWithPage() {
        List<ColumnInfo> columns = service.getColumnsByPage(COLUMN_TABLE, "test", 1, 1);
        assertThat(columns).hasSize(1);
        assertThat(columns.get(0).getColumn()).isEqualTo("1");
        assertThat(columns.get(0).getValue()).isEqualTo("1");
    }

    @Test
    public void testGetColumns() {
        assertThat(service.getColumns(BEAN_TABLE, "123"))
                .hasSize(4)
                .first()
                .isEqualToIgnoringNullFields(new ColumnInfo(null, "published", "true", null));
    }

    @Test
    public void testGetColumnObj() {
        ArticleBean bean = service.getColumnObj(COLUMN_OBJ_TABLE, "test", "a", ArticleBean.class);

        assertThat(bean.getRowkey()).isEqualTo("a");
        assertThat(bean.getTitle()).isEqualTo("测试a");
    }

    @Test
    public void testGetColumnObjList() {
        List<ArticleBean> beans = service.getPageColumnObjList(COLUMN_OBJ_TABLE, "test", 1, 1, ArticleBean.class);

        assertThat(beans).hasSize(1);
        assertThat(beans.get(0).getTitle()).isEqualTo("测试a");
    }

    @Test
    public void testGetRowKeys() {
        assertThat(service.getRowKeys(BEAN_TABLE))
                .hasSize(3)
                .contains("123", atIndex(0))
                .contains("124", atIndex(1));
    }

    @Test
    public void testGetRowKeysByPrefix() {
        assertThat(service.getRowKeysByPrefix(BEAN_TABLE, "13"))
                .hasSize(1)
                .contains("135", atIndex(0));
    }

    @Test
    public void testRowKeysPage() {
        assertThat(service.getRowKeys(BEAN_TABLE, "123", "124", 1))
                .hasSize(1)
                .contains("123", atIndex(0));
    }

    @Test
    public void testGetListWithRowKeys() {
        assertThat(service.getList(BEAN_TABLE, Lists.newArrayList("123", "124"), ArticleBean.class))
                .hasSize(2)
                .first()
                .isEqualToIgnoringNullFields(new ArticleBean("123", 1, "测试123", 2L, true));
    }

    @Test
    public void testGetList() {
        assertThat(service.getList(BEAN_TABLE, ArticleBean.class))
                .hasSize(3);
    }

    @Test
    public void testGetListFilter() {
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setColumn("title");
        columnInfo.setValue("测试");
        columnInfo.setCompareOperator(CompareOperator.NOT_EQUAL);
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        columnInfoList.add(columnInfo);

        List<ArticleBean> list = service.getList(BEAN_TABLE, Lists.newArrayList("123", "124"), null, columnInfoList,
                ArticleBean.class);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTitle()).isEqualTo("测试123");
    }

    @Test
    public void testGetPageList() {
        List<ArticleBean> list = service.getPageList(BEAN_TABLE, "123", "124", 1, ArticleBean.class);
        assertThat(list).hasSize(1);
    }

    @Test
    public void testDeleteRowKey() {
        String rowKey = "test2";
        String column = "2";

        assertThat(service.getSingleColumnValue(COLUMN_TABLE, rowKey, column)).isNotNull();
        service.delete(COLUMN_TABLE, rowKey);
        assertThat(service.getSingleColumnValue(COLUMN_TABLE, rowKey, column)).isNull();
    }

    @Test
    public void testDeleteColumn() {
        String rowKey = "test3";
        String column = "2";

        assertThat(service.getSingleColumnValue(COLUMN_TABLE, rowKey, column)).isNotNull();
        service.delete(COLUMN_TABLE, rowKey, column);
        assertThat(service.getSingleColumnValue(COLUMN_TABLE, rowKey, column)).isNull();
        assertThat(service.getSingleColumnValue(COLUMN_TABLE, rowKey, "1")).isNotNull();
    }

    @Test
    public void testAddCounter() {
        assertThat(service.addCounter(COLUMN_TABLE, "counter", "counter", 1)).isEqualTo(1);
        assertThat(service.addCounter(COLUMN_TABLE, "counter", "counter", 1)).isEqualTo(2);
        assertThat(service.addCounter(COLUMN_TABLE, "counter", "counter", 3)).isEqualTo(5);
        assertThat(service.addCounter(COLUMN_TABLE, "counter", "counter", -2)).isEqualTo(3);
    }
}
