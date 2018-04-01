package com.gaoxin.mop.config;

/**
 * Author: Mr.tan
 * Date:  2017/12/05 09:48
 * Description:
 */
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
