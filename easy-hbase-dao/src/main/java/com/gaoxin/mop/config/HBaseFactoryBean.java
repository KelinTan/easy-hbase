package com.gaoxin.mop.config;

import com.gaoxin.mop.constants.HBaseConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Mr.tan
 * Date:  2017/08/18
 * <p>
 * HBaseConstant 配置载入。初始化连接
 */
@Component
public class HBaseFactoryBean {

    private static HBaseFactoryBean factoryBean = null;

    private HBaseFactoryBean() {

    }

    public static HBaseFactoryBean getInstance() {
        if (factoryBean == null) {
            factoryBean = new HBaseFactoryBean();
        }
        return factoryBean;
    }

    private static List<HConnection> connections;

    private List<HBaseConfig> hbaseConfigs;

    public static void setConnections(List<HConnection> connections) {
        HBaseFactoryBean.connections = connections;
    }

    public void setHbaseConfigs(List<HBaseConfig> hbaseConfigs) {
        this.hbaseConfigs = hbaseConfigs;
    }

    public void initializeConnections() throws Exception {
        connections = new ArrayList<>();
        if (hbaseConfigs == null) {
            throw new RuntimeException("hbase config is null error");
        }
        for (HBaseConfig config : hbaseConfigs) {
            Configuration configuration = HBaseConfiguration.create();
            configuration.set("hbase.zookeeper.quorum", config.getZookeeperQuorum());
            configuration.set("hbase.zookeeper.property.clientPort", StringUtils.isBlank(config.getZookeeperClientPort()) ? HBaseConstant.DEFAULT_HBASE_PORT : config.getZookeeperClientPort());
            HConnection connection = HConnectionManager.createConnection(configuration);
            connections.add(connection);
        }

    }

    public static HConnection getDefaultConnection() {
        return connections.get(0);
    }

    public static HConnection getSpecifyConnection(int index) {
        if (index > connections.size() - 1) {
            throw new RuntimeException("hbase connection is not exist");
        }
        return connections.get(index);
    }
}
