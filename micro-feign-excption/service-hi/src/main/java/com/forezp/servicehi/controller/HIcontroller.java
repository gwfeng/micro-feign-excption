package com.forezp.servicehi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * author  fengguangwu
 * createTime  2021/4/7
 * desc
 **/
@RestController
public class HIcontroller {

    @RequestMapping("/hi")
    public String home(@RequestParam(value = "name", defaultValue = "ff") String name) throws Exception{
        if ("aaa".equals(name)){
            throw new Exception("内部异常测试");
        }
        return "hi " + name ;
    }
}
