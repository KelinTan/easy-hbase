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
