// Copyright 2020 Kelin Inc. All rights reserved.

package com.kelin.easy.hbase.systest;

import com.kelin.easy.hbase.common.bean.HBaseConnectionService;
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
