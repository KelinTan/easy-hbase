// Copyright 2020 Alo7 Inc. All rights reserved.

package com.kelin.easy.hbase.core;

import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;

/**
 * @author Kelin Tan
 */
public class FakeHBaseConnectionService implements HBaseConnectionService {
    @Override
    public Connection getConnection() throws IOException {
        return HBaseTestingUtilityManager.getInstance().getConnection();
    }
}
