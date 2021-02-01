// Copyright 2020 Kelin Inc. All rights reserved.

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
