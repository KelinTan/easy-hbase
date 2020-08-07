// Copyright 2020 Inc. All rights reserved.

package com.gaoxin.mop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kelin Tan
 */
@Configuration
@ConfigurationProperties(prefix = "hbase.config")
public class SpringBootHBaseConfig {
    private String zookeeperQuorum;
    private String zookeeperClientPort;


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
