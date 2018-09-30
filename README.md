> 新增码云地址：https://gitee.com/hanmov5/mop-hbase-template

## 一、写在前面

业务架构用到HBase，但由于某些不可名状原因，没有用phoniex等上层工具，开发都是用原生的HBase Api来实现逻辑，原生API虽然使用不算困难，但是在复用性和可读性方便很差，在这样的背景下，根据现有业务和现在HBase的常用方式上封装了这个简易的ORM，说是ORM其实不是特别准确，只能算是一个轻量级的工具框架吧，我把它称之为easy-hbase，现已在本人所在事业部广泛使用。

## 二、设计思路

> 基于现有HBase存储业务的逻辑和使用方式来设计工具的思路

1.  HBase使用列簇设计不宜过多，一般为单个固定列簇。
    
2.  HBase存储的基础数据表，比如某个订单或者某个帖子之类的，rowKey类似为主键，然后固定单个列簇里面，某个column就是基础数据的一个字段，value就是对应的值，这个实际上和关系型数据库有点类似了，这样我们需要一个封装，根据主键返回一堆字段，再映射成我们需要的对象。
    
3.  由于HBase是非关系型数据库，它的查询都是基于rowKey来进行的。一些关联查询需要建立相应的索引来实现（ps:复杂的HBase查询实现有多种方式，建立索引是比较常见的），比如某个用户的发帖列表，用户相关key为rowKey，column为Long最大值-发帖时间，value为帖子rowKey，这部分数据的column和value都不是固定的，区别于2的固定column值。
    
4.  HBase存储的基础单位也是字节，这点跟redis都是一致的，但是不同于redis客户端将value固定为String的字节数组，HBase提供的api是允许不同类型如Integer|Long|String等操作的，为方便管理和代码封装，实际业务上会规定尽量使用String来存储有关数据，特殊情况下用Long（主要是为了计数器的原子操作）。
    
5.  HBase基础数据表查询会返回指定PO，而一些索引表查询会返回不同的column和value，另外在条件查询时，我们有时候会限制返回我们需要的column或者是只取指定value（或者别的笔记关系：大于或不等于等）的column，我们需要一个基础的单元格类来承载这些功能。
    
6.  一般基础数据相同的属性，我们可能会放多份，区分正序或者倒叙等，还有复杂索引的数据其也没特定的table，所以我们设计的时候是将HBase的table以参数的形式传入，而非注解，建议这些配置在统一的地方维护
    

## 三、代码实现

> 下面会介绍框架的整体架构和核心相关类设计

1.  项目结构
    

 ![](https://static.oschina.net/uploads/space/2018/0401/145208_5ZIV_3057247.png)

-   easy-hbase-core：主要包括一些基础的Bean和Annotation，常量和工具类
-   easy-hbase-dao：主要代码，包括HBase相关操作代码封装和查询映射等，可直接spring集成使用
-   easy-hbase-spring-boot-starter：一个简单的spring-boot-starter，适合spring-boot项目集成使用
-   easy-hbase-spring-boot-demo：一个简单的spring-boot-demo项目，演示集成使用

2\. 核心类

-   HBaseColumn：Field注解，用于注解PO的相关属性 

```java
package com.gaoxin.mop.annotation;

import java.lang.annotation.*;

/**
 * Author: Mr.tan
 * Date:  2017/08/18
 * <p>
 * used on the field associate to the column from the hbase
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HBaseColumn {

    String family() default "";

    String column();

    boolean exist() default true;
}

```

>  family：列簇属性，用于指定特殊的列簇，项目里面有默认的全局family
> 
> column:：列属性，用于po属性和HBase对应column不匹配的情况，若一致无需指定
> 
> exist：若po中某个字段不在HBase存在的话，需手动设置这个属性为false，但建议po类为纯净的po

-    RowKey：field注解，用于注解po中的rowKey，若po中的属性不为rowkey值的话，需手动指定这个注解，否则将会默认field为rowkey

```java
package com.gaoxin.mop.annotation;

import java.lang.annotation.*;

/**
 * Author: Mr.tan
 * Date:  2017/08/18
 * <p>
 *     used on the field associate to the rowkey from the hbase
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RowKey {
}

```

-    ColumnInfo：封装最基础的单元格类，columnFamily，column和对应的value

```java
package com.gaoxin.mop.bean;


import com.gaoxin.mop.constants.HBaseConstant;
import org.apache.hadoop.hbase.filter.CompareFilter;

/**
 * Author: Mr.tan
 * Date:  2017/08/18
 * <p>
 * the base entity contains columnFamily and column、value、op
 * default value String.class,others set customize valueClass
 * used on limit the back columns and filter values
 * </p>
 */
public class ColumnInfo {

    private String columnFamily;
    private String column;
    private String value;
    private CompareFilter.CompareOp compareOperator;
    private Class valueClass;

    public ColumnInfo() {
    }

    public ColumnInfo(String column) {
        this(HBaseConstant.DEFAULT_FAMILY, column, CompareFilter.CompareOp.EQUAL);
    }

    public ColumnInfo(String columnFamily, String column, CompareFilter.CompareOp compareOperator) {
        this.columnFamily = columnFamily;
        this.column = column;
        this.compareOperator = compareOperator;
    }

    public ColumnInfo(String columnFamily, String column, CompareFilter.CompareOp compareOperator,Class valueClass) {
        this(columnFamily, column, compareOperator);
        this.valueClass = valueClass;
    }

    public ColumnInfo(String column, String value) {
        this(HBaseConstant.DEFAULT_FAMILY, column, value, CompareFilter.CompareOp.EQUAL);
    }

    public ColumnInfo(String columnFamily, String column, String value) {
        this(columnFamily, column, value, CompareFilter.CompareOp.EQUAL);
    }

    public ColumnInfo(String columnFamily, String column, String value, CompareFilter.CompareOp compareOperator) {

        this(columnFamily, column, compareOperator);
        this.value = value;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public CompareFilter.CompareOp getCompareOperator() {
        return compareOperator;
    }

    public void setCompareOperator(CompareFilter.CompareOp compareOperator) {
        this.compareOperator = compareOperator;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Class getValueClass() {
        return valueClass;
    }

    public void setValueClass(Class valueClass) {
        this.valueClass = valueClass;
    }
}

```

>  value：依据前文的设计思路，这里我们默认value为String类型，大多数情况下也应该这样做，如果有特殊的类型，如Long之类的，需指定valueClass的class
> 
> compareOperator：比较器属性，可以设置这个值用于在HBase限制返回column和值过滤的时候传入，可取的值：EQUAL|NOT EQUAL|GREATER等，我们这个类默认EQUAL

-   HBaseFactoryBean：HBase的连接初始化工厂Bean，用于初始化HBase连接

```java
package com.gaoxin.mop.config;

import com.gaoxin.mop.constants.HBaseConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Mr.tan
 * Date:  2017/08/18
 * <p>
 * HBaseConstant 配置载入。初始化连接
 */
@Component
public class HBaseFactoryBean {

    private static HBaseFactoryBean factoryBean = null;

    private HBaseFactoryBean() {

    }

    public static HBaseFactoryBean getInstance() {
        if (factoryBean == null) {
            factoryBean = new HBaseFactoryBean();
        }
        return factoryBean;
    }

    private static List<HConnection> connections;

    private List<HBaseConfig> hbaseConfigs;

    public static void setConnections(List<HConnection> connections) {
        HBaseFactoryBean.connections = connections;
    }

    public void setHbaseConfigs(List<HBaseConfig> hbaseConfigs) {
        this.hbaseConfigs = hbaseConfigs;
    }

    public void initializeConnections() throws Exception {
        connections = new ArrayList<>();
        if (hbaseConfigs == null) {
            throw new RuntimeException("hbase config is null error");
        }
        for (HBaseConfig config : hbaseConfigs) {
            Configuration configuration = HBaseConfiguration.create();
            configuration.set("hbase.zookeeper.quorum", config.getZookeeperQuorum());
            configuration.set("hbase.zookeeper.property.clientPort", StringUtils.isBlank(config.getZookeeperClientPort()) ? HBaseConstant.DEFAULT_HBASE_PORT : config.getZookeeperClientPort());
            HConnection connection = HConnectionManager.createConnection(configuration);
            connections.add(connection);
        }

    }

    public static HConnection getDefaultConnection() {
        return connections.get(0);
    }

    public static HConnection getSpecifyConnection(int index) {
        if (index > connections.size() - 1) {
            throw new RuntimeException("hbase connection is not exist");
        }
        return connections.get(index);
    }
}

```

-    HBaseDao：HBase基础操作核心类

```java
package com.gaoxin.mop.dao;


import com.gaoxin.mop.bean.ColumnInfo;

import java.util.List;

/**
 * Author: Mr.tan
 * Date:  2017/08/18
 */
public interface HBaseDao {

    <T> T get(String tableName, String rowKey, List<ColumnInfo> columns, List<ColumnInfo> filters, Class<? extends T> clazz);

    <T> T get(String tableName, String rowKey, Class<? extends T> clazz);

    <T> T get(String tableName, String rowKey, List<ColumnInfo> columns, Class<? extends T> clazz);

    String getSingleColumnValue(String tableName, String rowKey, String column);

    <T> T getSingleColumnValue(String tableName, String rowKey, String column, Class<? extends T> clazz);

    List<String> getRowKeys(String tableName);

    List<String> getRowKeys(String tableName, String startRow, String endRow);

    List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize, String separate, Integer index);

    List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize, String separate);

    List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize);

    List<String> getRowKeysByPrefix(String tableName, String prefix);

    List<String> getRowKeysByPrefix(String tableName, String startRow, String endRow, String prefix);

    List<ColumnInfo> getColumns(String tableName, String rowKey, String columnFamily, List<ColumnInfo> columns, List<ColumnInfo> filters);

    List<ColumnInfo> getColumns(String tableName, String rowKey, List<ColumnInfo> columns, List<ColumnInfo> filters);

    List<ColumnInfo> getColumns(String tableName, String rowKey, String columnFamily);

    List<ColumnInfo> getColumns(String tableName, String rowKey);

    <T> List<T> getList(String tableName, List<String> rowKeys, Class<? extends T> clazz);

    <T> List<T> getList(String tableName, List<String> rowKeys,List<ColumnInfo> columns, List<ColumnInfo> filters, Class<? extends T> clazz);

    <T> List<T> getList(String tableName, Class<? extends T> clazz);

    <T> List<T> getList(String tableName, List<ColumnInfo> columns, List<ColumnInfo> filters, Class<? extends T> clazz);

    <T> List<T> getList(String tableName, List<ColumnInfo> columns, List<ColumnInfo> filters, String start, String end, Class<? extends T> clazz);

    <T> List<T> getPageList(String tableName, String startRow, String endRow, Integer pageSize, Class<? extends T> clazz);

    List<ColumnInfo> getColumnsByPage(String tableName, String rowKey, Integer pageNo, Integer pageSize);

    List<ColumnInfo> getColumnsByPage(String tableName, String rowKey, Integer pageNo, Integer pageSize, List<ColumnInfo> columns, List<ColumnInfo> filters);

    <T> T getColumnObj(String tableName, String rowKey, String column,Class<? extends T> clazz);

    <T> List<T> getColumnObjList(String tableName, String rowKey, List<String> columns,Class<? extends T> clazz);

    <T> List<T> getPageColumnObjList(String tableName, String rowKey, Integer pageNo,Integer pageSize,Class<? extends T> clazz);

    <T> boolean put(String tableName, List<T> objects);

    <T> boolean put(String tableName, T object);

    boolean put(String tableName, String rowKey, String column, String value);

    boolean put(String tableName, String rowKey, ColumnInfo columnInfo);

    boolean put(String tableName, String rowKey, List<ColumnInfo> columnInfos);

    boolean delete(String tableName, String rowKey);

    boolean delete(String tableName, String rowKey, List<ColumnInfo> list);

    boolean delete(String tableName, String rowKey, ColumnInfo columnInfo);

    boolean delete(String tableName, String rowKey, String column);

    long addCounter(String tableName, String rowKey, String column, long num);
}

```

>  上述代码为主要核心代码，封装了总共八大类的方法：
> 
> 1.获取单个PO的get方法，column用于限制返回的column，filter用于批量过滤
> 
> 2.获取多个PO的getList方法
> 
> 3.获取单个signleColumn、多个Column的getColumns方法，支持分页（HBase的宽表分页，基于偏移量设计）
> 
> 4.支持批量PUT的put方法
> 
> 5.支持批量DELETE的delete方法
> 
> 6.支持原子操作的addCounter计数器方法
> 
> 7.支持只获取RowKey的分页方法（基于KeyOnlyFilter，减少数据传输，适用于仅需要RowKey情况）
> 
> 8.支持getColumsObj适用于value是一个json对象的查询方法

3\. 说明

> Retrieve an HTableInterface implementation for access to a table. The returned HTableInterface is not thread safe, a new instance should be created for each using thread. This is a lightweight operation, pooling or caching of the returned HTableInterface is neither required nor desired. Note that the HConnection needs to be unmanaged (created with [`HConnectionManager.createConnection(Configuration)`](http://hbase.apache.org/1.2/apidocs/org/apache/hadoop/hbase/client/HConnectionManager.html#createConnection(org.apache.hadoop.conf.Configuration))).

-   如上述引用，HBase官方推荐HConnecton全局维护，而HTablePool也被废弃，不建议使用，所以我们这里也是维护了全局的HConnection，在HTable的使用是即用即关的。
-   以上是主要的核心类，其主要映射也是通过反射来建立关系的，这里就不多说了
-   由于公司使用的hbase-client版本为0.96，所以这版本也只针对0.96，如果是更高版本的，由于部分api的改变暂不支持
-    在dao的模块里面，有相应的demo用例和对应的测试用例，测试用例写的也不规范，主要是当初内部快速开发校验下，可以作为一个验证。
-   spring-boot-starter版本也很简单，只是集成了一个扫描注入而已，也有相应的DEMO
-   最后，奉上源码地址，有不足的地方还望海涵，敬请斧正。

[https://github.com/Kelin92/easy-hbase](https://github.com/Kelin92/easy-hbase)
