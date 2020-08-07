// Copyright 2020 Inc. All rights reserved.

package com.gaoxin.mop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Kelin Tan
 */
@Configuration
@ConfigurationProperties(prefix = "multiple.hbase.config")
public class SpringBootHBaseConfigList {
    private List<String> zookeeperQuorum;
    private List<String> zookeeperClientPort;

    public List<String> getZookeeperQuorum() {
        return zookeeperQuorum;
    }

    public void setZookeeperQuorum(List<String> zookeeperQuorum) {
        this.zookeeperQuorum = zookeeperQuorum;
    }

    public List<String> getZookeeperClientPort() {
        return zookeeperClientPort;
    }

    public void setZookeeperClientPort(List<String> zookeeperClientPort) {
        this.zookeeperClientPort = zookeeperClientPort;
    }
}
