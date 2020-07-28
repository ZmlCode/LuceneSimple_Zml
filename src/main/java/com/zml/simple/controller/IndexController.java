package com.zml.simple.controller;

import com.zml.simple.define.Result;
import com.zml.simple.service.LuceneService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

/**
 * 上传文件建索
 * @author zml
 * @date 2020/07/24
 */
@Controller
public class IndexController {
    @Resource
    private LuceneService luceneService;

    /**
     * 简单的基于单字分词去建索
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload/file")
    @ResponseBody
    public Result index(@RequestParam(name = "file[]") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            String name = file.getOriginalFilename();
            if (luceneService.index(name, inputStream)) {
                return  Result.success("成功建索");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  Result.error("失败建索");
    }
}
