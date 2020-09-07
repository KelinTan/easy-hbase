// Copyright 2020 Inc. All rights reserved.

package com.kelin.easy.hbase;

import com.kelin.easy.hbase.common.constants.HBaseConstant;
import com.kelin.easy.hbase.core.HBaseService;
import com.kelin.easy.hbase.systest.HBaseTestingUtilityManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Kelin Tan
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HBaseTestAutoConfiguration.class)
public class HBaseServiceAutoConfigTest {
    private static final String DEMO_TABLE = "demo";

    @Autowired
    private HBaseService hBaseService;

    @BeforeClass
    public static void setUp() {
        HBaseTestingUtilityManager.createTable(DEMO_TABLE, HBaseConstant.DEFAULT_FAMILY);
    }

    @Test
    public void testGetRowKeys() {
        hBaseService.put(DEMO_TABLE, new Demo("1", 1, "name1"));

        Demo demo = hBaseService.get(DEMO_TABLE, "1", Demo.class);

        Assert.assertNotNull(demo);
        Assert.assertEquals("name1", demo.getName());
    }
}
