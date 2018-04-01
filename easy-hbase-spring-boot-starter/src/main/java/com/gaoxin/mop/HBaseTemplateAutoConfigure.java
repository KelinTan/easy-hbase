package com.gaoxin.mop;

import com.gaoxin.mop.config.HBaseConfig;
import com.gaoxin.mop.config.HBaseFactoryBean;
import com.gaoxin.mop.constants.HBaseConstant;
import com.gaoxin.mop.dao.HBaseDao;
import com.gaoxin.mop.dao.impl.HBaseDaoImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Mr.tan
 * Date:  2018/03/09 18:35
 * Description:配置基类
 */
@Configuration
@ConditionalOnClass(SpringBootHBaseConfig.class)
@EnableConfigurationProperties({SpringBootHBaseConfig.class, SpringBootHBaseConfigList.class})
public class HBaseTemplateAutoConfigure {

    @Autowired
    private SpringBootHBaseConfig springBootHBaseConfig;
    @Autowired
    private SpringBootHBaseConfigList springBootHBaseConfigList;

    @Bean
    public HBaseFactoryBean initializeHBaseFactoryBean() throws Exception {
        HBaseFactoryBean hBaseFactory = HBaseFactoryBean.getInstance();
        List<HBaseConfig> list = new ArrayList<>();
        //single HBase
        if (StringUtils.isNotBlank(springBootHBaseConfig.getZookeeperQuorum())) {
            HBaseConfig config = new HBaseConfig();
            config.setZookeeperQuorum(springBootHBaseConfig.getZookeeperQuorum());
            config.setZookeeperClientPort(StringUtils.isBlank(springBootHBaseConfig.getZookeeperClientPort()) ? HBaseConstant.DEFAULT_HBASE_PORT :
                    springBootHBaseConfig.getZookeeperClientPort());
            list.add(config);
        }
        //multiple HBase
        List<String> zookeeperConfigQuorums = springBootHBaseConfigList.getZookeeperQuorum();
        if (zookeeperConfigQuorums != null && zookeeperConfigQuorums.size() > 0) {
            for (int i = 0; i < zookeeperConfigQuorums.size(); i++) {
                HBaseConfig config = new HBaseConfig();
                config.setZookeeperQuorum(zookeeperConfigQuorums.get(i));
                String zookeeperClientPort = HBaseConstant.DEFAULT_HBASE_PORT;
                if (springBootHBaseConfigList.getZookeeperClientPort() != null && StringUtils.isNotBlank(springBootHBaseConfigList.getZookeeperClientPort().get(i))) {
                    zookeeperClientPort = springBootHBaseConfigList.getZookeeperClientPort().get(i);
                }
                config.setZookeeperClientPort(zookeeperClientPort);
                list.add(config);
            }
        }
        hBaseFactory.setHbaseConfigs(list);
        hBaseFactory.initializeConnections();
        return hBaseFactory;
    }

    @Bean
    @ConditionalOnMissingBean(HBaseDao.class)
    public HBaseDaoImpl getHBaseDao() {
        return new HBaseDaoImpl();
    }

}
