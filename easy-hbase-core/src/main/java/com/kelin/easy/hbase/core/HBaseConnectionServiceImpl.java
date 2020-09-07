// Copyright 2020 Kelin Tan Inc. All rights reserved.

package com.kelin.easy.hbase.core;

import com.kelin.easy.hbase.common.bean.HBaseConnectionService;
import com.kelin.easy.hbase.common.constants.HBaseConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

/**
 * @author Kelin Tan
 */
public class HBaseConnectionServiceImpl implements HBaseConnectionService {
    private final String zookeeperQuorum;
    private final String zookeeperClientPort;
    private volatile static Connection connection;

    public HBaseConnectionServiceImpl(String zookeeperQuorum, String zookeeperClientPort) {
        this.zookeeperQuorum = zookeeperQuorum;
        this.zookeeperClientPort = zookeeperClientPort;
    }

    @Override
    public Connection getConnection() throws IOException {
        if (connection == null) {
            Configuration configuration = HBaseConfiguration.create();
            configuration.set("hbase.zookeeper.quorum", zookeeperQuorum);
            configuration.set("hbase.zookeeper.property.clientPort",
                    StringUtils.isBlank(zookeeperClientPort) ? HBaseConstant.DEFAULT_HBASE_PORT
                            : zookeeperClientPort);
            connection = ConnectionFactory.createConnection(configuration);
        }

        return connection;
    }
}
