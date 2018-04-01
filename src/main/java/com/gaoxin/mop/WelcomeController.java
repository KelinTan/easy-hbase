package com.gaoxin.mop;

import com.gaoxin.mop.config.HBaseFactoryBean;
import com.gaoxin.mop.dao.HBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Author: Mr.tan
 * Date:  2017/09/05
 */

@RestController
@RequestMapping("/welcome")
public class WelcomeController {


    @Autowired
    private HBaseDao hBaseDao;

    @RequestMapping("hbase-demo")
    public String testHbaseDemo() {
        List<String> list = hBaseDao.getRowKeys("mopnovel_favoriteindex");
        System.out.println(list);
        System.out.println(HBaseFactoryBean.getSpecifyConnection(1));
        System.out.println(hBaseDao.get("mop_articles_desc","7703046885167257003",Article.class));
        return "hello";
    }
}
