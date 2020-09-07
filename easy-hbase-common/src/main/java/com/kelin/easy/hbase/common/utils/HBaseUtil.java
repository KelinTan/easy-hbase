package com.kelin.easy.hbase.common.utils;

import com.kelin.easy.hbase.common.annotation.HBaseColumn;
import com.kelin.easy.hbase.common.annotation.RowKey;
import com.kelin.easy.hbase.common.bean.ColumnInfo;
import com.kelin.easy.hbase.common.constants.HBaseConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.OperationWithAttributes;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * HBaseUtil 操作工具类
 *
 * @author Kelin Tan
 */
@SuppressWarnings("unchecked")
public class HBaseUtil {
    public static <T> List<Put> putObjectList(List<T> objectList) {

        if (objectList.isEmpty()) {
            return null;
        }
        List<Put> puts = new ArrayList<>();
        Put put = null;
        for (T obj : objectList) {
            Field[] fields = obj.getClass().getDeclaredFields();
            /*
             * 通过反射，用属性名称获得属性值
             */
            for (Field f : fields) {
                HBaseColumn hBaseColumn = f.getAnnotation(HBaseColumn.class);
                RowKey rowkey = f.getAnnotation(RowKey.class);
                if (rowkey != null || f.getName().equals(HBaseConstant.DEFAULT_ROWKEY_FIELD)) {
                    try {
                        byte[] rowBytes = getFieldValue(f, obj);
                        assert rowBytes != null;
                        put = new Put(rowBytes);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    continue;
                }
                //设置了hBaseColumn且exist=false
                if (hBaseColumn != null && !hBaseColumn.exist()) {
                    continue;
                }
                try {
                    byte[] value = getFieldValue(f, obj);
                    if (value != null) {
                        String vStr = Bytes.toString(value);
                        if (StringUtils.isNotBlank(vStr)) {
                            String columnFamily = HBaseConstant.DEFAULT_FAMILY;
                            String column = f.getName();
                            if (hBaseColumn != null) {
                                columnFamily = hBaseColumn.family();
                                column = hBaseColumn.column();
                            }
                            assert put != null;
                            put.addColumn(columnFamily.getBytes(), column.getBytes(), value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            puts.add(put);
        }
        return puts;
    }

    private static byte[] getFieldValue(Field field, Object object) throws Exception {
        String type = field.getGenericType().toString();
        //如果是Long类型
        if (type.equals("class java.lang.Long") || type.equals("long")) {
            Method m = object.getClass().getMethod("get" + getMethodName(field.getName()));
            Long val = (Long) m.invoke(object);
            if (val != null) {
                return Bytes.toBytes(val);

            }
        }

        // 如果类型是String
        if (type.equals("class java.lang.String")) { // 如果type是类类型，则前面包含"class "，后面跟类名
            Method m = object.getClass().getMethod("get" + getMethodName(field.getName()));
            String val = (String) m.invoke(object);// 调用getter方法获取属性值
            if (val != null) {
                return Bytes.toBytes(val);
            }
        }
        // 如果类型是Integer
        if (type.equals("class java.lang.Integer") || type.equals("int")) {
            Method m = object.getClass().getMethod("get" + getMethodName(field.getName()));
            Integer val = (Integer) m.invoke(object);
            if (val != null) {
                return String.valueOf(val).getBytes();
            }
        }
        // 如果类型是Double
        if (type.equals("class java.lang.Double") || type.equals("double")) {
            Method m = object.getClass().getMethod("get" + getMethodName(field.getName()));
            Double val = (Double) m.invoke(object);
            if (val != null) {
                return String.valueOf(val).getBytes();
            }
        }

        //如果是Byte类型
        if (type.equals("class java.lang.Byte") || type.equals("byte")) {
            Method m = object.getClass().getMethod("get" + getMethodName(field.getName()));
            Byte val = (Byte) m.invoke(object);
            if (val != null) {
                return String.valueOf(val).getBytes();
            }
        }
        //如果是Boolean类型
        if (type.equals("class java.lang.Boolean") || type.equals("boolean")) {
            Method m = object.getClass().getMethod("get" + getMethodName(field.getName()));
            Boolean val = (Boolean) m.invoke(object);
            if (val != null) {
                return String.valueOf(val).getBytes();
            }
        }
        return null;

    }

    private static String getMethodName(String fieldName) throws Exception {
        byte[] items = fieldName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }

    /**
     * 对象映射---通过注解和反射
     *
     * @param clazz -
     * @param result -
     * @param <T> -
     * @return -
     */
    public static <T> T parseObject(Class<T> clazz, Result result)
            throws IllegalAccessException, InstantiationException {
        Field[] fields = clazz.getDeclaredFields();
        T instance = clazz.newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            //设置RowKey:若带注解RowKey.class 取注解，否则默认field
            if (field.isAnnotationPresent(RowKey.class) || field.getName().equals(HBaseConstant.DEFAULT_ROWKEY_FIELD)) {
                field.set(instance, convert(field.getType(), result.getRow()));
            } else {
                //设置其余属性
                if (field.isAnnotationPresent(HBaseColumn.class)) {
                    HBaseColumn hbaseColumn = field.getAnnotation(HBaseColumn.class);
                    if (hbaseColumn.exist()) {
                        String family = StringUtils.isBlank(hbaseColumn.family()) ? HBaseConstant.DEFAULT_FAMILY
                                : hbaseColumn.family();
                        String column = StringUtils.isBlank(hbaseColumn.column()) ? field.getName()
                                : hbaseColumn.column();
                        field.set(instance,
                                convert(field.getType(), result.getValue(family.getBytes(), column.getBytes())));
                    }
                } else {
                    field.set(instance, convert(field.getType(),
                            result.getValue(HBaseConstant.DEFAULT_FAMILY.getBytes(), field.getName().getBytes())));
                }
            }
        }
        return instance;
    }

    /**
     * 转换属性--根据类型
     * <p>
     * 暂时只处理基本数据类型
     * </p>
     *
     * @param clazz -
     * @param values -
     * @param <T> -
     * @return -
     */
    public static <T> T convert(Class<T> clazz, byte[] values) {
        if (values == null) {
            return null;
        }
        String classType = clazz.toString();
        String value = new String(values);
        T instance;
        switch (classType) {
            case "class java.lang.String":
            case "char":
                instance = (T) value;
                break;
            case "int":
            case "class java.lang.Integer":
                instance = (T) Integer.valueOf(value);
                break;
            case "class java.lang.Long":
            case "long":
                instance = (T) Long.valueOf(Bytes.toLong(values));
                break;
            case "class java.lang.Double":
            case "double":
                instance = (T) Double.valueOf(value);
                break;
            case "class java.lang.Float":
            case "float":
                instance = (T) Float.valueOf(value);
                break;
            case "class java.lang.Byte":
            case "byte":
                instance = (T) Byte.valueOf(value);
                break;
            case "class java.lang.Short":
            case "short":
                instance = (T) Short.valueOf(value);
                break;
            case "class java.lang.Boolean":
            case "boolean":
                instance = (T) Boolean.valueOf(value);
                break;
            default:
                return null;
        }
        return instance;
    }

    /**
     * 设置operation 如:Get/Scan等的filter过滤和返回column
     *
     * @param operation -
     */
    public static void setColumnAndFilter(OperationWithAttributes operation, List<ColumnInfo> columns,
            List<ColumnInfo> filters) {
        //限制返回column
        if (columns != null) {
            for (ColumnInfo columnInfo : columns) {
                if (operation instanceof Scan) {
                    ((Scan) operation).addColumn(columnInfo.getColumnFamily().getBytes(),
                            columnInfo.getColumn().getBytes());
                } else if (operation instanceof Get) {
                    ((Get) operation).addColumn(columnInfo.getColumnFamily().getBytes(),
                            columnInfo.getColumn().getBytes());
                }
            }
        }
        //过滤器
        if (filters != null) {
            FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            for (ColumnInfo columnInfo : filters) {
                //设置默认的ColumnFamily和CompareOp
                CompareOperator compareOp = CompareOperator.EQUAL;
                if (null != columnInfo.getCompareOperator()) {
                    compareOp = columnInfo.getCompareOperator();
                }
                String columnFamily = HBaseConstant.DEFAULT_FAMILY;
                if (StringUtils.isNotBlank(columnInfo.getColumnFamily())) {
                    columnFamily = columnInfo.getColumnFamily();
                }
                if (null != columnInfo.getValue()) {
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(columnFamily.getBytes(),
                            columnInfo.getColumn().getBytes(), compareOp,
                            HBaseUtil.getValueBytes(columnInfo.getValue(), columnInfo.getValueClass()));
                    filter.setFilterIfMissing(true);
                    filterList.addFilter(filter);
                } else {
                    QualifierFilter filter = new QualifierFilter(columnInfo.getCompareOperator(),
                            new BinaryComparator(Bytes.toBytes(columnFamily)));
                    filterList.addFilter(filter);
                }

            }
            if (operation instanceof Scan) {
                ((Scan) operation).setFilter(filterList);
            } else if (operation instanceof Get) {
                ((Get) operation).setFilter(filterList);
            }
        }
    }

    /**
     * 获取value的bytes[] value可以是object类型
     *
     * @param value 原value
     * @return byte[]
     */
    public static byte[] getValueBytes(String value, Class valueClass) {
        assert value != null;
        if (valueClass != null && valueClass.getName().equals("java.lang.Long")) { //用于计数器的字节
            return Bytes.toBytes(Long.valueOf(value));
        } else {
            return value.getBytes();
        }
    }

}
