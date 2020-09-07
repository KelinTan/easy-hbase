// Copyright 2020 Kelin Tan Inc. All rights reserved.

package com.kelin.easy.hbase.common.bean;

import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;

/**
 * @author Kelin Tan
 */
public interface HBaseConnectionService {
    Connection getConnection() throws IOException;
}
