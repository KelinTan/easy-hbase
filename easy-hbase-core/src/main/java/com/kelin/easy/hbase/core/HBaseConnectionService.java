// Copyright 2020 Alo7 Inc. All rights reserved.

package com.kelin.easy.hbase.core;

import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;

/**
 * @author Kelin Tan
 */
public interface HBaseConnectionService {
    Connection getConnection() throws IOException;
}
