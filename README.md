![Java CI](https://github.com/KelinTan/easy-hbase/workflows/Java%20CI/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/KelinTan/easy-hbase/branch/master/graph/badge.svg?token=yFWkvtCuld)](undefined)

## 脚手架系列 
[脚手架 All in one](https://github.com/KelinTan/spring-boot-archetype)

[纯净的脚手架 Test For Java](https://github.com/KelinTan/spring-boot-archetype-test)

[纯净的脚手架 Test For Kotlin](https://github.com/KelinTan/spring-boot-archetype-test-kotlin)

## 2.0.0版本 2020.8.10 更新

1. 重新梳理项目结构
2. 升级HBase版本到2.0.0
3. 支持HBaseTest用于单元测试 && 集成测试
4. 升级SpringBoot版本2.0.9
5. Gradle构建替换Maven
6. 梳理代码.重写初始化逻辑



## 一、写在前面

业务架构用到HBase，但由于某些不可名状原因，没有用phoniex等上层工具，开发都是用原生的HBase Api来实现逻辑，原生API虽然使用不算困难，但是在复用性和可读性方便很差，在这样的背景下，根据现有业务和现在HBase的常用方式上封装了这个简易的ORM，说是ORM其实不是特别准确，只能算是一个轻量级的工具框架吧

## 二、设计思路

> 基于现有HBase存储业务的逻辑和使用方式来设计工具的思路

1.  HBase使用列簇设计不宜过多，一般为单个固定列簇。
    
2.  HBase存储的基础数据表，比如某个订单或者某个帖子之类的，rowKey类似为主键，然后固定单个列簇里面，某个column就是基础数据的一个字段，value就是对应的值，这个实际上和关系型数据库有点类似了，这样我们需要一个封装，根据主键返回一堆字段，再映射成我们需要的对象。
    
3.  由于HBase是非关系型数据库，它的查询都是基于rowKey来进行的。一些关联查询需要建立相应的索引来实现（ps:复杂的HBase查询实现有多种方式，建立索引是比较常见的），比如某个用户的发帖列表，用户相关key为rowKey，column为Long最大值-发帖时间，value为帖子rowKey，这部分数据的column和value都不是固定的，区别于2的固定column值。
    
4.  HBase存储的基础单位也是字节，这点跟redis都是一致的，但是不同于redis客户端将value固定为String的字节数组，HBase提供的api是允许不同类型如Integer|Long|String等操作的，为方便管理和代码封装，实际业务上会规定尽量使用String来存储有关数据，特殊情况下用Long（主要是为了计数器的原子操作）。
    
5.  HBase基础数据表查询会返回指定PO，而一些索引表查询会返回不同的column和value，另外在条件查询时，我们有时候会限制返回我们需要的column或者是只取指定value（或者别的比较关系：大于或不等于等）的column，我们需要一个基础的单元格类来承载这些功能。
    
6.  一般基础数据相同的属性，我们可能会放多份，区分正序或者倒叙等，还有复杂索引的数据其也没特定的table，所以我们设计的时候是将HBase的table以参数的形式传入，而非注解，建议这些配置在统一的地方维护
    

## 三、代码实现

> 下面会介绍框架的整体架构和核心相关类设计

1.  项目结构
    
-   easy-hbase-common: 通用module
-   easy-hbase-core： 核心service
-   easy-hbase-spring-boot-starter：一个简单的spring-boot-starter，适合spring-boot项目集成使用
-   easy-habse-systest 系统测试module,集成`HBaseTestingUtility`用于测试

2\. 核心类

-   HBaseColumn：Field注解，用于注解PO的相关属性 

```java
package com.kelin.easy.hbase.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Kelin Tan
 *         <p>
 *         used on the field associate to the column from the hbase
 *         </p>
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
package com.kelin.easy.hbase.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Kelin Tan
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
package com.kelin.easy.hbase.common.bean;

import com.kelin.easy.hbase.common.constants.HBaseConstant;
import org.apache.hadoop.hbase.CompareOperator;

/**
 * @author Kelin Tan
 *         <p>
 *         the base entity contains columnFamily and column、value、op default value String.class,others set customize
 *         valueClass used on limit the back columns and filter values
 *         </p>
 */
public class ColumnInfo {
    private String columnFamily;
    private String column;
    private String value;
    private CompareOperator compareOperator;
    private Class<?> valueClass;
}

```

>  value：依据前文的设计思路，这里我们默认value为String类型，大多数情况下也应该这样做，如果有特殊的类型，如Long之类的，需指定valueClass的class
> 
> compareOperator：比较器属性，可以设置这个值用于在HBase限制返回column和值过滤的时候传入，可取的值：EQUAL|NOT EQUAL|GREATER等，我们这个类默认EQUAL

```

-    HBaseDao：HBase基础操作核心类

```java

package com.kelin.easy.hbase.core;

import com.kelin.easy.hbase.common.bean.ColumnInfo;

import java.util.List;

/**
 * @author Kelin Tan Date:  2017/08/18
 */
public interface HBaseService {
    /**
     * Get column value as object by rowKey with columns and filters
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     * @param columns query columns
     * @param filters filter specific columns
     * @param clazz the column values as clazz
     */
    <T> T get(String tableName, String rowKey, List<ColumnInfo> columns, List<ColumnInfo> filters,
            Class<? extends T> clazz);

    /**
     * Get column value as object by rowKey
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     * @param clazz the column values as clazz
     */
    <T> T get(String tableName, String rowKey, Class<? extends T> clazz);

    /**
     * Get column value as object by rowKey with columns
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     * @param columns query columns
     * @param clazz the column values as clazz
     */
    <T> T get(String tableName, String rowKey, List<ColumnInfo> columns, Class<? extends T> clazz);

    /**
     * Get single column value as string by rowKey and column
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     * @param column specific column
     */
    String getSingleColumnValue(String tableName, String rowKey, String column);

    /**
     * Get single column value as class by rowKey and column
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     * @param column specific column
     * @param clazz the column values as clazz
     */
    <T> T getSingleColumnValue(String tableName, String rowKey, String column, Class<? extends T> clazz);

    /**
     * Get all rowKeys
     *
     * @param tableName HBaseTable name
     */
    List<String> getRowKeys(String tableName);

    /**
     * Get rowKeys with startRow and endRow
     *
     * @param tableName HBaseTable name
     * @param startRow the start row
     * @param endRow the end row
     */
    List<String> getRowKeys(String tableName, String startRow, String endRow);

    /**
     * Get rowKeys with startRow,endRow and size limit,support separate
     *
     * @param tableName HBaseTable name
     * @param startRow the start row
     * @param endRow the end row
     * @param separate separate symbol to split rowKey,just used for row combined like "_" and so on
     * @param index separate index to retain index row
     */
    List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize, String separate,
            Integer index);

    /**
     * Get rowKeys with startRow,endRow and size limit,support separate and default index
     *
     * @param tableName HBaseTable name
     * @param startRow the start row
     * @param endRow the end row
     * @param separate separate symbol to split rowKey,just used for row combined like "_" and so on
     */
    List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize, String separate);

    /**
     * Get rowKeys with startRow,endRow and size limit
     *
     * @param tableName HBaseTable name
     * @param startRow the start row
     * @param endRow the end row
     * @param pageSize limit size
     */
    List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize);

    /**
     * Get rowKeys with prefix with PrefixFilter
     *
     * @param tableName HBaseTable name
     * @param prefix rowKey prefix
     */
    List<String> getRowKeysByPrefix(String tableName, String prefix);

    /**
     * Get rowKeys with prefix with PrefixFilter,and startRow,endRow
     *
     * @param tableName HBaseTable name
     * @param startRow the start row
     * @param endRow the end row
     * @param prefix rowKey prefix
     */
    List<String> getRowKeysByPrefix(String tableName, String startRow, String endRow, String prefix);

    /**
     * Get columns with row and columnFamily,support limit columns and filters
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     * @param columns query columns
     * @param columnFamily the column family
     * @param filters filter specific columns use filter
     */
    List<ColumnInfo> getColumns(String tableName, String rowKey, String columnFamily, List<ColumnInfo> columns,
            List<ColumnInfo> filters);

    /**
     * Get columns with row and default columnFamily,support limit columns and filters
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     * @param columns query columns
     * @param filters filter specific columns use filter
     */
    List<ColumnInfo> getColumns(String tableName, String rowKey, List<ColumnInfo> columns, List<ColumnInfo> filters);

    /**
     * Get columns with row and columnFamily,without columns and filters
     *
     * @param tableName HBaseTable name
     * @param columnFamily the column family
     * @param rowKey HBaseTable rowKey
     */
    List<ColumnInfo> getColumns(String tableName, String rowKey, String columnFamily);

    /**
     * Get columns with row and default columnFamily
     *
     * @param tableName HBaseTable name
     * @param rowKey HBaseTable rowKey
     */
    List<ColumnInfo> getColumns(String tableName, String rowKey);

    /**
     * Get list object with rowKeys
     *
     * @param tableName HBaseTable name
     * @param rowKeys HBaseTable rowKey list
     * @param clazz column value as clazz
     */
    <T> List<T> getList(String tableName, List<String> rowKeys, Class<? extends T> clazz);

    /**
     * Get list object with rowKeys and columns,filters
     *
     * @param tableName HBaseTable name
     * @param rowKeys HBaseTable rowKey list
     * @param columns query columns
     * @param filters filter specific columns use filter
     * @param clazz column value as clazz
     */
    <T> List<T> getList(String tableName, List<String> rowKeys, List<ColumnInfo> columns, List<ColumnInfo> filters,
            Class<? extends T> clazz);

    /**
     * Get list object all,not recommended
     *
     * @param tableName HBaseTable name
     * @param clazz column value as clazz
     */
    <T> List<T> getList(String tableName, Class<? extends T> clazz);

    /**
     * Get list object all,not recommended,with columns and filters
     *
     * @param tableName HBaseTable name
     * @param columns query columns
     * @param filters filter specific columns use filter
     * @param clazz column value as clazz
     */
    <T> List<T> getList(String tableName, List<ColumnInfo> columns, List<ColumnInfo> filters, Class<? extends T> clazz);

    /**
     * Get list object all,not recommended,with columns,filters,startRow and endRow
     *
     * @param tableName HBaseTable name
     * @param columns query columns
     * @param filters filter specific columns use filter
     * @param startRow the start row
     * @param endRow the end row
     * @param clazz column value as clazz
     */
    <T> List<T> getList(String tableName, List<ColumnInfo> columns, List<ColumnInfo> filters, String startRow,
            String endRow, Class<? extends T> clazz);

    /**
     * Get page list object,support page size limit
     *
     * @param tableName HBaseTable name
     * @param startRow the row start
     * @param endRow the row end
     * @param pageSize limit size
     * @param clazz column value as clazz
     */
    <T> List<T> getPageList(String tableName, String startRow, String endRow, Integer pageSize,
            Class<? extends T> clazz);

    /**
     * Get page columns by row,support page size limit
     *
     * @param tableName HBaseTable name
     * @param rowKey the row
     * @param pageNo the pageNo
     * @param pageSize limit size
     */
    List<ColumnInfo> getColumnsByPage(String tableName, String rowKey, Integer pageNo, Integer pageSize);

    /**
     * Get page columns by row,support page size limit
     *
     * @param tableName HBaseTable name
     * @param rowKey the row
     * @param pageNo the pageNo
     * @param pageSize limit size
     * @param columns query columns
     * @param filters column filters
     */
    List<ColumnInfo> getColumnsByPage(String tableName, String rowKey, Integer pageNo, Integer pageSize,
            List<ColumnInfo> columns, List<ColumnInfo> filters);

    /**
     * Get specific column value as class
     *
     * @param tableName HBaseTable name
     * @param rowKey the row
     * @param column the column
     * @param clazz column values as class
     */
    <T> T getColumnObj(String tableName, String rowKey, String column, Class<? extends T> clazz);

    /**
     * Get list column value as class
     *
     * @param tableName HBaseTable name
     * @param rowKey the row
     * @param columns the columns
     * @param clazz column values as class
     */
    <T> List<T> getColumnObjList(String tableName, String rowKey, List<String> columns, Class<? extends T> clazz);

    /**
     * Get page column value as class
     *
     * @param tableName HBaseTable name
     * @param rowKey the row
     * @param pageNo the pageNo
     * @param pageSize limit size
     * @param clazz column values as class
     */
    <T> List<T> getPageColumnObjList(String tableName, String rowKey, Integer pageNo, Integer pageSize,
            Class<? extends T> clazz);

    /**
     * Put list object with not null fields
     *
     * @param tableName HBaseTable name
     * @param objects put data list
     */
    <T> boolean put(String tableName, List<T> objects);

    /**
     * Put single object with not null fields
     *
     * @param tableName HBaseTable name
     * @param object put data
     */
    <T> boolean put(String tableName, T object);

    /**
     * Put single column value
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     * @param column put column
     * @param value put value
     */
    boolean put(String tableName, String rowKey, String column, String value);

    /**
     * Put single column value
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     * @param columnInfo put column info object
     */
    boolean put(String tableName, String rowKey, ColumnInfo columnInfo);

    /**
     * Put list column value
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     * @param columns put column info object list
     */
    boolean put(String tableName, String rowKey, List<ColumnInfo> columns);

    /**
     * delete by row
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     */
    boolean delete(String tableName, String rowKey);

    /**
     * delete list columns by row
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     * @param list delete column info object list
     */
    boolean delete(String tableName, String rowKey, List<ColumnInfo> list);

    /**
     * delete single column by row
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     * @param columnInfo delete column info object
     */
    boolean delete(String tableName, String rowKey, ColumnInfo columnInfo);

    /**
     * delete single column by row
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     * @param column delete column
     */
    boolean delete(String tableName, String rowKey, String column);

    /**
     * add counter,atomic operation
     *
     * @param tableName HBaseTable name
     * @param rowKey put row
     * @param column update column
     * @param num add num
     */
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
-   基于`HBaseTestingUtility` 提供基于内存的HBase集群集成测试，使用可参考对应测试用例

4.使用

- 可以直接引入easy-hbase-core初始化对应连接后使用
- spring-boot可以引入对应的starter和配置hbase基础连接信息即可
