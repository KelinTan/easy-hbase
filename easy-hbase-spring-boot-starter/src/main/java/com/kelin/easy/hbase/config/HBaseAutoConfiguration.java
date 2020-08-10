// Copyright 2020 Kelin Tan Inc. All rights reserved.

package com.kelin.easy.hbase.config;

import com.kelin.easy.hbase.core.HBaseConnectionService;
import com.kelin.easy.hbase.core.HBaseConnectionServiceImpl;
import com.kelin.easy.hbase.dao.HBaseService;
import com.kelin.easy.hbase.dao.impl.HBaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kelin Tan
 */
@Configuration
@ConditionalOnClass(HBaseConfig.class)
@EnableConfigurationProperties(HBaseConfig.class)
public class HBaseAutoConfiguration {
    @Autowired
    private HBaseConfig hBaseConfig;

    @Bean
    @ConditionalOnMissingBean(HBaseService.class)
    public HBaseService createHBaseService() {
        HBaseConnectionService connectionService = new HBaseConnectionServiceImpl(hBaseConfig.getZookeeperQuorum(),
                hBaseConfig.getZookeeperClientPort());
        return new HBaseServiceImpl(connectionService);
    }
}
