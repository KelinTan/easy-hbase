package com.kelin.easy.hbase.core;


import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.kelin.easy.hbase.common.bean.ColumnInfo;
import com.kelin.easy.hbase.common.bean.HBaseConnectionService;
import com.kelin.easy.hbase.common.constants.HBaseConstant;
import com.kelin.easy.hbase.common.json.JsonConverter;
import com.kelin.easy.hbase.common.utils.HBaseUtil;
import org.apache.commons.collections.CollectionUtils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Kelin Tan
 */
public class HBaseServiceImpl implements HBaseService {
    private final HBaseConnectionService connectionService;

    private static final Logger logger = LoggerFactory.getLogger(HBaseServiceImpl.class);

    public HBaseServiceImpl(HBaseConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public <T> T get(String tableName, String rowKey, List<ColumnInfo> columns, List<ColumnInfo> filters,
            Class<? extends T> clazz) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(rowKey);

        T instance = null;
        try (HTable hTable = getTable(tableName)) {
            Get get = new Get(rowKey.getBytes());
            HBaseUtil.setColumnAndFilter(get, columns, filters);
            Result rs = hTable.get(get);
            if (!rs.isEmpty()) {
                instance = HBaseUtil.parseObject(clazz, rs);
            }

        } catch (Exception e) {
            logError(e);
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
        Preconditions.checkNotNull(column);
        Preconditions.checkNotNull(clazz);

        T t = null;
        try (HTable hTable = getTable(tableName)) {
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addColumn(HBaseConstant.DEFAULT_FAMILY.getBytes(), column.getBytes());
            Result result = hTable.get(get);
            for (Cell cell : result.rawCells()) {
                t = HBaseUtil.convert(clazz, CellUtil.cloneValue(cell));
            }
        } catch (Exception e) {
            logError(e);
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
    public List<String> getRowKeys(String tableName, String startRow, String endRow) {
        return getRowKeysByPrefix(tableName, startRow, endRow, null);
    }

    @Override
    public List<String> getRowKeys(String tableName, String startRow, String endRow, Integer pageSize, String separate,
            Integer index) {
        List<String> rowKeys = new ArrayList<>();

        Scan scan = getRowScan(startRow, endRow,
                new FilterList(FilterList.Operator.MUST_PASS_ALL, new KeyOnlyFilter()));

        try (HTable hTable = getTable(tableName); ResultScanner scanner = hTable.getScanner(scan)) {
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
            logError(e);
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
    public List<String> getRowKeysByPrefix(String tableName, String startRow, String endRow, String prefix) {
        List<String> rowKeys = new ArrayList<>();

        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        Filter kof = new KeyOnlyFilter();
        if (StringUtils.isNotBlank(prefix)) {
            Filter prefixFilter = new PrefixFilter(prefix.getBytes());
            filterList.addFilter(prefixFilter);
        }
        filterList.addFilter(kof);
        Scan scan = getRowScan(startRow, endRow, filterList);

        try (HTable hTable = getTable(tableName); ResultScanner scanner = hTable.getScanner(scan)) {
            for (Result result : scanner) {
                if (!result.isEmpty()) {
                    rowKeys.add(new String(result.getRow()));
                }
            }
        } catch (IOException e) {
            logError(e);
        }
        return rowKeys;
    }

    @Override
    public List<ColumnInfo> getColumns(String tableName, String rowKey, String columnFamily, List<ColumnInfo> columns,
            List<ColumnInfo> filters) {
        List<ColumnInfo> dataList = new ArrayList<>();
        try (HTable hTable = getTable(tableName)) {
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addFamily(columnFamily.getBytes());
            HBaseUtil.setColumnAndFilter(get, columns, filters);
            Result result = hTable.get(get);

            for (Cell cell : result.rawCells()) {
                String column = new String(CellUtil.cloneQualifier(cell), Charsets.UTF_8.name());
                String value = new String(CellUtil.cloneValue(cell), Charsets.UTF_8.name());
                ColumnInfo bean = new ColumnInfo();
                bean.setColumn(column);
                bean.setValue(value);
                dataList.add(bean);
            }
        } catch (IOException e) {
            logError(e);
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
        if (clazz == null || CollectionUtils.isEmpty(rowKeys)) {
            return null;
        }
        List<T> resultList = new ArrayList<>();
        try (HTable hTable = getTable(tableName)) {
            ArrayList<Get> getList = new ArrayList<>();

            for (String rowKey : rowKeys) {
                if (StringUtils.isNotBlank(rowKey)) {
                    Get get = new Get(rowKey.getBytes());
                    HBaseUtil.setColumnAndFilter(get, columns, filters);
                    getList.add(get);
                }
            }
            Result[] results = hTable.get(getList);
            for (Result result : results) {
                if (!result.isEmpty()) {
                    T instance = HBaseUtil.parseObject(clazz, result);
                    resultList.add(instance);
                }
            }

        } catch (Exception e) {
            logError(e);
        }
        return resultList;
    }

    @Override
    public <T> List<T> getList(String tableName, Class<? extends T> clazz) {
        return getList(tableName, null, null, clazz);
    }

    @Override
    public <T> List<T> getList(String tableName, List<ColumnInfo> columns, List<ColumnInfo> filters,
            Class<? extends T> clazz) {
        return getList(tableName, columns, filters, null, null, clazz);
    }

    @Override
    public <T> List<T> getList(String tableName, List<ColumnInfo> columns, List<ColumnInfo> filters, String startRow,
            String endRow, Class<? extends T> clazz) {
        Preconditions.checkNotNull(clazz);

        List<T> list = new ArrayList<>();
        Scan scan = getRowScan(startRow, endRow, null);

        try (HTable hTable = getTable(tableName); ResultScanner scanner = hTable.getScanner(scan)) {
            HBaseUtil.setColumnAndFilter(scan, columns, filters);
            for (Result rs : scanner) {
                if (!rs.isEmpty()) {
                    T instance = HBaseUtil.parseObject(clazz, rs);
                    list.add(instance);
                }
            }
        } catch (Exception e) {
            logError(e);
        }
        return list;
    }

    @Override
    public <T> List<T> getPageList(String tableName, String startRow, String endRow, Integer pageSize,
            Class<? extends T> clazz) {
        Preconditions.checkNotNull(clazz);

        List<T> list = new ArrayList<>();
        Scan scan = getRowScan(startRow, endRow, null);
        try (HTable hTable = getTable(tableName); ResultScanner scanner = hTable.getScanner(scan)) {
            scan.setMaxResultSize(pageSize);
            for (Result rs : scanner) {
                if (!rs.isEmpty()) {
                    T instance = HBaseUtil.parseObject(clazz, rs);
                    list.add(instance);
                }
            }
        } catch (Exception e) {
            logError(e);
        }
        return list;
    }

    @Override
    public List<ColumnInfo> getColumnsByPage(String tableName, String rowKey, Integer pageNo, Integer pageSize) {
        return getColumnsByPage(tableName, rowKey, pageNo, pageSize, null, null);
    }

    @Override
    public List<ColumnInfo> getColumnsByPage(String tableName, String rowKey, Integer pageNo, Integer pageSize,
            List<ColumnInfo> columns, List<ColumnInfo> filters) {
        List<ColumnInfo> dataList = new ArrayList<>();
        try (HTable hTable = getTable(tableName)) {

            Get get = new Get(Bytes.toBytes(rowKey));
            HBaseUtil.setColumnAndFilter(get, columns, filters);
            if (pageNo != null && pageNo != 0 && pageSize != null && pageSize != 0) {
                get.setMaxResultsPerColumnFamily(pageSize);
                get.setRowOffsetPerColumnFamily((pageNo - 1) * pageSize);
            }

            Result result = hTable.get(get);
            for (Cell cell : result.rawCells()) {
                String column = new String(CellUtil.cloneQualifier(cell), Charsets.UTF_8.name());
                String value = new String(CellUtil.cloneValue(cell), Charsets.UTF_8.name());
                ColumnInfo bean = new ColumnInfo();
                bean.setColumn(column);
                bean.setValue(value);
                dataList.add(bean);
            }
        } catch (Exception e) {
            logError(e);
        }
        return dataList;
    }

    @Override
    public <T> T getColumnObj(String tableName, String rowKey, String column, Class<? extends T> clazz) {
        Preconditions.checkNotNull(column);

        List<T> list = getColumnObjList(tableName, rowKey, Collections.singletonList(column), clazz);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public <T> List<T> getColumnObjList(String tableName, String rowKey, List<String> columns,
            Class<? extends T> clazz) {
        List<T> dataList = new ArrayList<>();
        try (HTable hTable = getTable(tableName)) {
            Get get = new Get(Bytes.toBytes(rowKey));
            if (CollectionUtils.isNotEmpty(columns)) {
                for (String column : columns) {
                    get.addColumn(HBaseConstant.DEFAULT_FAMILY.getBytes(), column.getBytes());
                }
            }
            Result result = hTable.get(get);

            for (Cell cell : result.rawCells()) {
                String value = new String(CellUtil.cloneValue(cell), Charsets.UTF_8.name());
                dataList.add(JsonConverter.deserialize(value, clazz));
            }
        } catch (Exception e) {
            logError(e);
        }
        return dataList;
    }

    @Override
    public <T> List<T> getPageColumnObjList(String tableName, String rowKey, Integer pageNo, Integer pageSize,
            Class<? extends T> clazz) {
        List<T> dataList = new ArrayList<>();
        try (HTable hTable = getTable(tableName)) {
            Get get = new Get(Bytes.toBytes(rowKey));
            if (pageNo != null && pageNo != 0 && pageSize != null && pageSize != 0) {
                get.setMaxResultsPerColumnFamily(pageSize);
                get.setRowOffsetPerColumnFamily((pageNo - 1) * pageSize);
            }
            Result result = hTable.get(get);

            for (Cell cell : result.rawCells()) {
                String value = new String(CellUtil.cloneValue(cell), Charsets.UTF_8.name());
                dataList.add(JsonConverter.deserialize(value, clazz));
            }
        } catch (Exception e) {
            logError(e);
        }
        return dataList;
    }

    @Override
    public <T> boolean put(String tableName, List<T> objects) {
        try (HTable hTable = getTable(tableName)) {
            List<Put> puts = HBaseUtil.putObjectList(objects);
            if (CollectionUtils.isNotEmpty(puts)) {
                hTable.put(puts);
            }
            return true;
        } catch (Exception e) {
            logError(e);
        }
        return false;
    }

    @Override
    public <T> boolean put(String tableName, T object) {
        Preconditions.checkNotNull(object, "Error obj is null ");

        return put(tableName, Collections.singletonList(object));
    }

    @Override
    public boolean put(String tableName, String rowKey, String column, String value) {
        ColumnInfo columnInfo = new ColumnInfo(HBaseConstant.DEFAULT_FAMILY, column, value);
        return put(tableName, rowKey, columnInfo);
    }

    @Override
    public boolean put(String tableName, String rowKey, ColumnInfo columnInfo) {
        Preconditions.checkNotNull(columnInfo);

        return put(tableName, rowKey, Collections.singletonList(columnInfo));
    }

    @Override
    public boolean put(String tableName, String rowKey, List<ColumnInfo> columns) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(columns), "Column info should have value");

        try (HTable hTable = getTable(tableName)) {
            Put put = new Put(rowKey.getBytes());
            for (ColumnInfo columnInfo : columns) {
                put.addColumn(columnInfo.getColumnFamily().getBytes(), columnInfo.getColumn().getBytes(),
                        HBaseUtil.getValueBytes(columnInfo.getValue(), columnInfo.getValueClass()));
            }
            hTable.put(put);
            return true;
        } catch (Exception e) {
            logError(e);
        }
        return false;
    }

    @Override
    public boolean delete(String tableName, String rowKey) {
        Preconditions.checkNotNull(rowKey, "row key is null");

        try (HTable hTable = getTable(tableName)) {
            Delete del = new Delete(rowKey.getBytes());
            hTable.delete(del);
            return true;
        } catch (Exception e) {
            logError(e);
        }
        return false;
    }

    @Override
    public boolean delete(String tableName, String rowKey, List<ColumnInfo> columns) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(columns), "Column info should have value");

        try (HTable hTable = getTable(tableName)) {
            Delete del = new Delete(rowKey.getBytes());
            for (ColumnInfo columnInfo : columns) {
                String defaultFamily = HBaseConstant.DEFAULT_FAMILY;
                if (StringUtils.isNotBlank(columnInfo.getColumnFamily())) {
                    defaultFamily = columnInfo.getColumnFamily();
                }
                del.addColumn(defaultFamily.getBytes(), columnInfo.getColumn().getBytes());
            }
            hTable.delete(del);
            return true;
        } catch (Exception e) {
            logError(e);
        }
        return false;
    }

    @Override
    public boolean delete(String tableName, String rowKey, ColumnInfo columnInfo) {
        Preconditions.checkNotNull(columnInfo, "Error,obj is null ");

        return delete(tableName, rowKey, Collections.singletonList(columnInfo));
    }

    @Override
    public boolean delete(String tableName, String rowKey, String column) {
        ColumnInfo columnInfo = new ColumnInfo(column);
        return delete(tableName, rowKey, columnInfo);
    }

    @Override
    public long addCounter(String tableName, String rowKey, String column, long num) {
        try (HTable hTable = getTable(tableName)) {
            return hTable.incrementColumnValue(rowKey.getBytes(), HBaseConstant.DEFAULT_FAMILY.getBytes(),
                    column.getBytes(), num);
        } catch (Exception e) {
            logError(e);
        }
        return -1;
    }

    private Scan getRowScan(String startRow, String endRow, Filter filter) {
        Scan scan = new Scan();
        if (StringUtils.isNotBlank(startRow)) {
            scan.withStartRow(startRow.getBytes());
        }
        if (StringUtils.isNotBlank(endRow)) {
            scan.withStopRow(endRow.getBytes());
        }
        if (filter != null) {
            scan.setFilter(filter);
        }

        return scan;
    }

    private void logError(Exception e) {
        logger.error("HBase Exception: ", e);
    }

    private HTable getTable(String tableName) throws IOException {
        return (HTable) connectionService.getConnection().getTable(TableName.valueOf(tableName));
    }

}
