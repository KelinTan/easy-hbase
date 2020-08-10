package com.kelin.easy.hbase.utils;


import org.apache.commons.lang3.StringUtils;

/**
 * @author Kelin Tan
 */
public class RowKeyUtil {

    public static String getRotateRowKey(String cts, String rdts) {
        return StringUtils.reverse(cts + rdts);
    }

    public static String getDescRowKey(String cts, String rdts) {
        long value = Long.MAX_VALUE - Long.parseLong(cts + rdts);
        return String.valueOf(value);
    }

    public static String getRotateRowKey(String ctsrdts) {
        return StringUtils.reverse(ctsrdts);
    }

    public static String getRotate(String ctsrdts) {
        return StringUtils.reverse(ctsrdts);
    }

    public static String getDescRowKey(String ctsrdts) {
        long value = Long.MAX_VALUE - Long.parseLong(ctsrdts);
        return String.valueOf(value);
    }

    public static String getRdts() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
