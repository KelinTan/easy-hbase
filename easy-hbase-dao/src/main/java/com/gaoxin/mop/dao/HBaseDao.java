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
