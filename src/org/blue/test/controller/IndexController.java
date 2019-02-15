package org.blue.test.controller;

import org.blue.framework.annotation.Autowired;
import org.blue.framework.annotation.Component;
import org.blue.framework.annotation.RequestMapping;
import org.blue.test.service.IndexService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 浅蓝
 * @email blue@ixsec.org
 * @since 2019/2/15 16:57
 */
@Component
@RequestMapping("/test")
public class IndexController {

    @Autowired
    private IndexService indexService;


    @RequestMapping("hello")
    public String hello(HttpServletRequest req){

        System.out.println(req);
        return "hello";
    }
}
