// Copyright 2020 Kelin Inc. All rights reserved.

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

    public ColumnInfo() {
    }

    public ColumnInfo(String column) {
        this(HBaseConstant.DEFAULT_FAMILY, column, CompareOperator.EQUAL);
    }

    public ColumnInfo(String columnFamily, String column, CompareOperator compareOperator) {
        this.columnFamily = columnFamily;
        this.column = column;
        this.compareOperator = compareOperator;
    }

    public ColumnInfo(String columnFamily, String column, CompareOperator compareOperator, Class<?> valueClass) {
        this(columnFamily, column, compareOperator);
        this.valueClass = valueClass;
    }

    public ColumnInfo(String column, String value) {
        this(HBaseConstant.DEFAULT_FAMILY, column, value, CompareOperator.EQUAL);
    }

    public ColumnInfo(String columnFamily, String column, String value) {
        this(columnFamily, column, value, CompareOperator.EQUAL);
    }

    public ColumnInfo(String columnFamily, String column, String value, CompareOperator compareOperator) {
        this(columnFamily, column, compareOperator);
        this.value = value;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public CompareOperator getCompareOperator() {
        return compareOperator;
    }

    public void setCompareOperator(CompareOperator compareOperator) {
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

    public Class<?> getValueClass() {
        return valueClass;
    }

    public void setValueClass(Class<?> valueClass) {
        this.valueClass = valueClass;
    }
}
