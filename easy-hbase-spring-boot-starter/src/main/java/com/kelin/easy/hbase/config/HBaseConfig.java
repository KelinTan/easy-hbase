// Copyright 2020 Kelin Inc. All rights reserved.

package com.kelin.easy.hbase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kelin
 */
@Configuration
@ConfigurationProperties(prefix = "easy.hbase")
public class HBaseConfig {
    private String zookeeperQuorum;
    private String zookeeperClientPort;

    public HBaseConfig() {
    }

    public HBaseConfig(String zookeeperQuorum, String zookeeperClientPort) {
        this.zookeeperQuorum = zookeeperQuorum;
        this.zookeeperClientPort = zookeeperClientPort;
    }

    public String getZookeeperQuorum() {
        return zookeeperQuorum;
    }

    public void setZookeeperQuorum(String zookeeperQuorum) {
        this.zookeeperQuorum = zookeeperQuorum;
    }

    public String getZookeeperClientPort() {
        return zookeeperClientPort;
    }

    public void setZookeeperClientPort(String zookeeperClientPort) {
        this.zookeeperClientPort = zookeeperClientPort;
    }
}
