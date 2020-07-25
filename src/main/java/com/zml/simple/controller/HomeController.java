package com.zml.simple.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 程序运行状态
 * @author zml
 * @date 2020/7/24
 */
@Controller
public class HomeController {
    /**
     * 可以通过这个接口看项目是否部署成功
     * @return
     */
    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "home，run ok";
    }
}
