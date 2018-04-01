package com.gaoxin.mop;

import com.gaoxin.mop.constants.HBaseConstant;
import com.mop.core.corebase.spring.MopBootStarterListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Class Name: MopStarterPlugin
 * Create Date: 18-3-21 下午3:15
 * Creator: Chim·Zigui
 * Version: v1.0
 * Updater:
 * Date Time:
 * Description:
 */
public class MopHbaseStarterListener implements MopBootStarterListener {

    private Map<String, Object> initInformation = new HashMap<String, Object>();

    /**
     * 返回Starter的名称
     *
     * @return Starter 名称
     */
    @Override
    public String starterName() {
        return "mop-hbase-spring-boot-starter";
    }

    /**
     * 收集Starter的配置信息,会在启动配置数据中进行组装和接口页面显示
     * <p>
     * 该方法的执行,在 onFinished 之后,可以在onFinished之前对数据进行初始化收集
     *
     * @return 配置信息
     */
    @Override
    public Map<String, Object> starterInfoCollect() {
        // Step 4
        return initInformation;
    }

    /**
     * Spring Starting后事件,可预处理Spring加载前
     */
    @Override
    public void onStarting() {
        // Step 1
    }

    /**
     * Spring Context准备完成事件
     *
     * @param context ApplicationContext
     */
    @Override
    public void onPrepared(ApplicationContext context) {
        // Step 2
    }

    /**
     * Spring 初始化完成,所有就绪
     *
     * @param context ApplicationContext
     */
    @Override
    public void onFinished(ApplicationContext context) {
        // Step 3
        SpringBootHBaseConfig hBaseConfig = context.getBean(SpringBootHBaseConfig.class);
        if (hBaseConfig != null) {
            initInformation.put("zookeeper-quorum", hBaseConfig.getZookeeperQuorum());
            initInformation.put("zookeeper-port", StringUtils.isBlank(hBaseConfig.getZookeeperClientPort()) ?
                    HBaseConstant.DEFAULT_HBASE_PORT : hBaseConfig.getZookeeperClientPort());
        }
    }
}
