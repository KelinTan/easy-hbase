package com.kelin.easy.hbase.dao.impl;


import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.kelin.easy.hbase.bean.ColumnInfo;
import com.kelin.easy.hbase.constants.HBaseConstant;
import com.kelin.easy.hbase.core.HBaseConnectionService;
import com.kelin.easy.hbase.dao.HBaseService;
import com.kelin.easy.hbase.utils.HBaseUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kelin Tan
 */
@SuppressWarnings("all")
public class HBaseServiceImpl implements HBaseService {
    private HBaseConnectionService connectionService;

    private static Logger logger = LoggerFactory.getLogger(HBaseServiceImpl.class);

    public HBaseServiceImpl(HBaseConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public <T> T get(String tableName, String rowKey, @Nullable List<ColumnInfo> columns,
            @Nullable List<ColumnInfo> filters, Class<? extends T> clazz) {
        if (clazz == null || StringUtils.isBlank(rowKey)) {
            return null;
        }
        HTable hTable = null;
        T instance = null;
        try {
            hTable = getTable(tableName);
            Get get = new Get(rowKey.getBytes());
            HBaseUtil.setColumnAndFilter(get, columns, filters);
            Result rs = hTable.get(get);
            if (!rs.isEmpty()) {
                instance = HBaseUtil.parseObject(clazz, rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("hbase table close error", e);
            }
        }
        return instance;
    }

    @Override
    public <T> T get(String tableName, String rowKey, Class<? extends T> clazz) {
        return get(tableName, rowKey, null, null, clazz);
    }

    @Override
    public <T> T get(String tableName, String rowKey, List<ColumnInfo> columns, Class<? extends T> clazz) {
        return get(tableName, rowKey, columns, null, clazz);
    }

    @Override
    public String getSingleColumnValue(String tableName, String rowKey, String column) {
        return getSingleColumnValue(tableName, rowKey, column, String.class);
    }

    @Override
    public <T> T getSingleColumnValue(String tableName, String rowKey, String column, Class<? extends T> clazz) {
        if (StringUtils.isBlank(column)) {
            return null;
        }
        HTable hTable = null;
        T t = null;
        try {
            hTable = getTable(tableName);
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addColumn(HBaseConstant.DEFAULT_FAMILY.getBytes(), column.getBytes());
            Result result = hTable.get(get);
            for (Cell cell : result.rawCells()) {
                t = HBaseUtil.convert(clazz, CellUtil.cloneValue(cell));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                hTable.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return t;
    }

    @Override
    public List<String> getRowKeysByPrefix(String tableName, String prefix) {
        return getRowKeysByPrefix(tableName, null, null, prefix);
    }


    @Override
    public List<String> getRowKeys(String tableName) {
        return getRowKeys(tableName, null, null);
    }

    @Override
    public List<String> getRowKeys(String tableName, String start, String end) {
        return getRowKeysByPrefix(tableName, start, end, null);
    }

    @Override
    public List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize, String separate,
            Integer index) {
        List<String> rowKeys = new ArrayList<>();
        HTable hTable = null;
        ResultScanner scanner = null;
        try {
            hTable = getTable(tableName);
            Scan scan = new Scan();
            FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            if (StringUtils.isNotBlank(startRow)) {
                scan.setStartRow(startRow.getBytes());
            }
            if (StringUtils.isNotBlank(endRow)) {
                scan.setStopRow(endRow.getBytes());
            }
            Filter kof = new KeyOnlyFilter();
            filterList.addFilter(kof);
            scan.setFilter(filterList);
            scanner = hTable.getScanner(scan);
            for (Result result : scanner) {
                if (!result.isEmpty()) {
                    String rowKey = new String(result.getRow());
                    if (StringUtils.isNotBlank(separate)) {
                        rowKeys.add(rowKey.split(separate)[index]);
                    } else {
                        rowKeys.add(rowKey);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
                }
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return rowKeys;
    }

    @Override
    public List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize,
            String separate) {
        return getRowKeys(tableName, startRow, endRow, pageSize, separate, 1);
    }

    @Override
    public List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize) {
        return getRowKeys(tableName, startRow, endRow, pageSize, null);
    }

    @Override
    public List<String> getRowKeysByPrefix(String tableName, String start, String end, String prefix) {
        List<String> rowKeys = new ArrayList<>();
        HTable hTable = null;
        ResultScanner scanner = null;
        try {
            hTable = getTable(tableName);
            Scan scan = new Scan();
            if (StringUtils.isNotBlank(start)) {
                scan.setStartRow(start.getBytes());
            }
            if (StringUtils.isNotBlank(end)) {
                scan.setStopRow(end.getBytes());
            }
            FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            Filter kof = new KeyOnlyFilter();
            if (StringUtils.isNotBlank(prefix)) {
                Filter prefixFilter = new PrefixFilter(prefix.getBytes());
                filterList.addFilter(prefixFilter);
            }
            filterList.addFilter(kof);
            scan.setFilter(filterList);
            scanner = hTable.getScanner(scan);
            for (Result result : scanner) {
                if (!result.isEmpty()) {
                    rowKeys.add(new String(result.getRow()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
                }
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return rowKeys;
    }

    @Override
    public List<ColumnInfo> getColumns(String tableName, String rowKey, String columnFamily, List<ColumnInfo> columns,
            List<ColumnInfo> filters) {
        HTable hTable = null;
        List<ColumnInfo> dataList = new ArrayList<>();
        try {
            hTable = getTable(tableName);
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addFamily(columnFamily.getBytes());
            HBaseUtil.setColumnAndFilter(get, columns, filters);
            Result result = hTable.get(get);

            for (Cell cell : result.rawCells()) {
                String column = new String(CellUtil.cloneQualifier(cell), "utf-8");
                String value = new String(CellUtil.cloneValue(cell), "utf-8");
                ColumnInfo bean = new ColumnInfo();
                bean.setColumn(column);
                bean.setValue(value);
                dataList.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                hTable.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return dataList;
    }

    @Override
    public List<ColumnInfo> getColumns(String tableName, String rowKey, List<ColumnInfo> columns,
            List<ColumnInfo> filters) {
        return getColumns(tableName, rowKey, HBaseConstant.DEFAULT_FAMILY, columns, null);
    }

    @Override
    public List<ColumnInfo> getColumns(String tableName, String rowKey, String columnFamily) {
        return getColumns(tableName, rowKey, columnFamily, null, null);
    }

    @Override
    public List<ColumnInfo> getColumns(String tableName, String rowKey) {
        return getColumns(tableName, rowKey, HBaseConstant.DEFAULT_FAMILY);
    }

    @Override
    public <T> List<T> getList(String tableName, List<String> rowKeys, Class<? extends T> clazz) {
        return getList(tableName, rowKeys, null, null, clazz);
    }

    @Override
    public <T> List<T> getList(String tableName, List<String> rowKeys, List<ColumnInfo> columns,
            List<ColumnInfo> filters, Class<? extends T> clazz) {
        if (clazz == null || rowKeys == null || rowKeys.size() == 0) {
            return null;
        }
        HTable hTable = null;
        List<T> resultList = new ArrayList<>();
        try {
            ArrayList<Get> getlist = new ArrayList<>();
            hTable = getTable(tableName);
            for (String rowKey : rowKeys) {
                if (StringUtils.isNotBlank(rowKey)) {
                    Get get = new Get(rowKey.getBytes());
                    HBaseUtil.setColumnAndFilter(get, columns, filters);
                    getlist.add(get);
                }
            }
            Result[] resultsset = hTable.get(getlist);
            for (Result results : resultsset) {
                if (!results.isEmpty()) {
                    T instance = HBaseUtil.parseObject(clazz, results);
                    resultList.add(instance);
                } else {
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return resultList;
    }

    @Override
    public <T> List<T> getList(String tableName, Class<? extends T> clazz) {
        return getList(tableName, null, null, clazz);
    }

    @Override
    public <T> List<T> getList(String tableName, @Nullable List<ColumnInfo> columns, @Nullable List<ColumnInfo> filters,
            Class<? extends T> clazz) {
        return getList(tableName, columns, filters, null, null, clazz);
    }

    @Override
    public <T> List<T> getList(String tableName, List<ColumnInfo> columns, List<ColumnInfo> filters, String start,
            String end, Class<? extends T> clazz) {
        if (clazz == null) {
            return null;
        }
        HTable hTable = null;
        List<T> list = new ArrayList<>();
        try {
            hTable = getTable(tableName);
            Scan scan = new Scan();
            if (StringUtils.isNotBlank(start)) {
                scan.setStartRow(start.getBytes());
            }
            if (StringUtils.isNotBlank(end)) {
                scan.setStopRow(end.getBytes());
            }
            HBaseUtil.setColumnAndFilter(scan, columns, filters);
            ResultScanner scanner = hTable.getScanner(scan);
            for (Result rs : scanner) {
                if (!rs.isEmpty()) {
                    T instance = HBaseUtil.parseObject(clazz, rs);
                    list.add(instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return list;
    }

    @Override
    public <T> List<T> getPageList(String tableName, String startRow, String endRow, Integer pageSize,
            Class<? extends T> clazz) {
        if (clazz == null) {
            return null;
        }
        HTable hTable = null;
        List<T> list = new ArrayList<>();
        try {
            hTable = getTable(tableName);
            Scan scan = new Scan();
            if (StringUtils.isNotBlank(startRow)) {
                scan.setStartRow(startRow.getBytes());
            }
            if (StringUtils.isNotBlank(endRow)) {
                scan.setStopRow(endRow.getBytes());
            }
            scan.setMaxResultSize(pageSize);
            ResultScanner scanner = hTable.getScanner(scan);
            for (Result rs : scanner) {
                if (!rs.isEmpty()) {
                    T instance = HBaseUtil.parseObject(clazz, rs);
                    list.add(instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return list;
    }

    @Override
    public List<ColumnInfo> getColumnsByPage(String tableName, String rowkey, Integer pageNo, Integer pageSize) {
        return getColumnsByPage(tableName, rowkey, pageNo, pageSize, null, null);
    }

    @Override
    public List<ColumnInfo> getColumnsByPage(String tableName, String rowkey, Integer pageNo, Integer pageSize,
            List<ColumnInfo> columns, List<ColumnInfo> filters) {
        HTable hTable = null;
        List<ColumnInfo> dataList = new ArrayList<>();
        try {
            hTable = getTable(tableName);
            Get get = new Get(Bytes.toBytes(rowkey));
            HBaseUtil.setColumnAndFilter(get, columns, filters);
            if (pageNo != null && pageNo != 0 && pageSize != 0 && pageSize != null) {
                get.setMaxResultsPerColumnFamily(pageSize);
                get.setRowOffsetPerColumnFamily((pageNo - 1) * pageSize);
            }
            Result result = hTable.get(get);

            for (Cell cell : result.rawCells()) {
                String column = new String(CellUtil.cloneQualifier(cell), "utf-8");
                String value = new String(CellUtil.cloneValue(cell), "utf-8");
                ColumnInfo bean = new ColumnInfo();
                bean.setColumn(column);
                bean.setValue(value);
                dataList.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                hTable.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return dataList;
    }

    @Override
    public <T> T getColumnObj(String tableName, String rowKey, String column, Class<? extends T> clazz) {
        if (StringUtils.isBlank(column)) {
            return null;
        }
        List<T> list = getColumnObjList(tableName, rowKey, Lists.<String>newArrayList(column), clazz);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public <T> List<T> getColumnObjList(String tableName, String rowKey, List<String> columns,
            Class<? extends T> clazz) {
        HTable hTable = null;
        List<T> dataList = new ArrayList<>();
        try {
            hTable = getTable(tableName);
            Get get = new Get(Bytes.toBytes(rowKey));
            if (columns != null && columns.size() > 0) {
                for (String column : columns) {
                    get.addColumn(HBaseConstant.DEFAULT_FAMILY.getBytes(), column.getBytes());
                }
            }
            Result result = hTable.get(get);

            for (Cell cell : result.rawCells()) {
                String value = new String(CellUtil.cloneValue(cell), "utf-8");
                dataList.add(JSONObject.parseObject(value, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                hTable.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return dataList;
    }

    @Override
    public <T> List<T> getPageColumnObjList(String tableName, String rowKey, Integer pageNo, Integer pageSize,
            Class<? extends T> clazz) {
        HTable hTable = null;
        List<T> dataList = new ArrayList<>();
        try {
            hTable = getTable(tableName);
            Get get = new Get(Bytes.toBytes(rowKey));
            if (pageNo != null && pageNo != 0 && pageSize != 0 && pageSize != null) {
                get.setMaxResultsPerColumnFamily(pageSize);
                get.setRowOffsetPerColumnFamily((pageNo - 1) * pageSize);
            }
            Result result = hTable.get(get);

            for (Cell cell : result.rawCells()) {
                String value = new String(CellUtil.cloneValue(cell), "utf-8");
                dataList.add(JSONObject.parseObject(value, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get hbase data error", e);
        } finally {
            try {
                hTable.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return dataList;
    }

    @Override
    public <T> boolean put(String tableName, List<T> objects) {
        boolean isSucess = false;
        HTable hTable = null;
        try {
            List<Put> puts = HBaseUtil.putObjectList(objects);
            hTable = getTable(tableName);
            if (puts != null && puts.size() > 0) {
                hTable.put(puts);
            }
            isSucess = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("put hbase data error", e);
        } finally {
            try {
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return isSucess;
    }

    @Override
    public <T> boolean put(String tableName, T object) {
        Preconditions.checkNotNull(object, "Error obj is null ");
        return put(tableName, Lists.newArrayList(object));
    }

    @Override
    public boolean put(String tableName, String rowKey, String column, String value) {
        ColumnInfo columnInfo = new ColumnInfo(HBaseConstant.DEFAULT_FAMILY, column, value);
        return put(tableName, rowKey, columnInfo);
    }

    @Override
    public boolean put(String tableName, String rowKey, ColumnInfo columnInfo) {
        Preconditions.checkArgument(columnInfo != null, "Column info should have value");
        return put(tableName, rowKey, Lists.<ColumnInfo>newArrayList(columnInfo));
    }

    @Override
    public boolean put(String tableName, String rowKey, List<ColumnInfo> columnInfos) {
        Preconditions.checkArgument(columnInfos != null && columnInfos.size() > 0, "Column info should have value");
        HTable hTable = null;
        boolean isSuccess = false;
        try {
            hTable = getTable(tableName);
            Put put = new Put(rowKey.getBytes());
            for (ColumnInfo columnInfo : columnInfos) {
                put.addColumn(columnInfo.getColumnFamily().getBytes(), columnInfo.getColumn().getBytes(),
                        HBaseUtil.getValueBytes(columnInfo.getValue(), columnInfo.getValueClass()));
            }
            hTable.put(put);

            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("put hbase data error", e);
        } finally {
            try {
                if (hTable != null) {
                    hTable.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return isSuccess;
    }

    @Override
    public boolean delete(String tableName, String rowKey) {
        Preconditions.checkNotNull(rowKey, "row key is null");
        HTable htab = null;
        boolean isSuccess = false;
        try {
            htab = getTable(tableName);
            Delete del = new Delete(rowKey.getBytes());
            htab.delete(del);
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("delete hbase data error", e);
        } finally {
            try {
                if (htab != null) {
                    htab.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return isSuccess;
    }

    @Override
    public boolean delete(String tableName, String rowKey, List<ColumnInfo> columnInfos) {
        Preconditions.checkArgument(columnInfos != null && columnInfos.size() > 0, "column info should have value");
        HTable htab = null;
        boolean isSuccess = false;
        try {
            htab = getTable(tableName);
            Delete del = new Delete(rowKey.getBytes());
            for (ColumnInfo columnInfo : columnInfos) {
                String defaultFamily = HBaseConstant.DEFAULT_FAMILY;
                if (StringUtils.isNotBlank(columnInfo.getColumnFamily())) {
                    defaultFamily = columnInfo.getColumnFamily();
                }
                del.addColumn(defaultFamily.getBytes(), columnInfo.getColumn().getBytes());
            }
            htab.delete(del);
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("delete hbase data error", e);
        } finally {
            try {
                if (htab != null) {
                    htab.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("close hbase table error", e);
            }
        }
        return isSuccess;
    }

    @Override
    public boolean delete(String tableName, String rowKey, ColumnInfo columnInfo) {
        Preconditions.checkNotNull(columnInfo, "error,obj is null ");
        return delete(tableName, rowKey, Lists.<ColumnInfo>newArrayList(columnInfo));
    }

    @Override
    public boolean delete(String tableName, String rowKey, String column) {
        ColumnInfo columnInfo = new ColumnInfo(column);
        return delete(tableName, rowKey, columnInfo);
    }

    @Override
    public long addCounter(String tableName, String rowKey, String column, long num) {
        HTable hTable = null;
        long result = -1;
        try {
            hTable = getTable(tableName);
            result = hTable.incrementColumnValue(rowKey.getBytes(), HBaseConstant.DEFAULT_FAMILY.getBytes(),
                    column.getBytes(), num);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("add hbase counter error", e);
        } finally {
            if (hTable != null) {
                try {
                    hTable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("close hbase table error", e);
                }
            }
        }
        return result;
    }

    private HTable getTable(String tableName) throws IOException {
        return (HTable) connectionService.getConnection().getTable(TableName.valueOf(tableName));
    }

}
