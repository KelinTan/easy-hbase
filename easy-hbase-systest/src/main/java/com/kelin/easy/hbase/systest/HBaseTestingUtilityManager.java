// Copyright 2020 Kelin Tan Inc. All rights reserved.

package com.kelin.easy.hbase.systest;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kelin Tan
 */
public class HBaseTestingUtilityManager {
    private static final Logger logger = LoggerFactory.getLogger(HBaseTestingUtilityManager.class);

    private static final HBaseTestingUtility utility;

    private static final Map<String, Boolean> TABLE_MAP = new HashMap<>();

    static {
        Configuration config = HBaseConfiguration.create();
        //fix issue https://issues.apache.org/jira/browse/HBASE-20544?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aall-tabpanel
        config.setInt(HConstants.REGIONSERVER_PORT, 0);
        utility = new HBaseTestingUtility(config);
        try {
            utility.startMiniCluster();
        } catch (Exception e) {
            logger.error("StartMiniCluster error", e);
        }
    }

    public static HBaseTestingUtility getInstance() {
        return utility;
    }

    public static void createTable(String tableName, String family) {
        if (TABLE_MAP.containsKey(tableName)) {
            return;
        }
        try {
            utility.createTable(TableName.valueOf(tableName), family);
            TABLE_MAP.putIfAbsent(tableName, Boolean.TRUE);
        } catch (IOException e) {
            logger.error("CreateTable error", e);
        }
    }
}