package com.zml.simple.controller;

import com.zml.simple.define.Result;
import com.zml.simple.define.SearchData;
import com.zml.simple.service.LuceneService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.sql.Date;

/**
 * 查询关键词检索
 *    基于StandAnalyzer单字分词的检索
 * @author zml
 * @date 2020/07/24
 */
@Controller
public class SimpleQueryController {
    @Resource
    private LuceneService luceneService;

    /**
     * 查询出关键词所在文档
     *    注意：检索时使用StandAnalyzer做的是单字分词 搜索时必须输入小写 输入一句话搜索不出来的  这个点可以使用luke8.0工具分析看到
     * @param value 关键词
     * @return
     */
    @RequestMapping(value = "/query/word/{value}")
    @ResponseBody
    public Result search(@PathVariable(name = "value") String value) {
        if (!luceneService.isValid()) {
            return Result.success("索引数据为空");
        }
        SearchData result = luceneService.search(value);
        if (result == null) {
            return Result.error("检索失败");
        }
        return  Result.success("成功检索", result);
    }

    /**
     * 按时间范围查建索的文档
     *   仅支持2020-05-03这一种日期格式 暂不容参数格式错误
     * @param begin
     * @param end
     * @return
     */
    @RequestMapping(value = "/query/date/from/{begin}/to/{end}")
    @ResponseBody
    public Result search(@PathVariable(name = "begin") String begin,
                         @PathVariable(name = "end") String end){

        if (!luceneService.isValid()) {
            return Result.success("索引数据为空");
        }
        long start;
        long last;
        try {
            start = Date.valueOf(begin).getTime();
            last = Date.valueOf(end).getTime();
        } catch (Exception e) {
            return Result.error("日期参数非法仅支持2020-05-03格式");
        }
        SearchData result = luceneService.search(start, last);
        if (result == null) {
            return Result.error("检索失败");
        }
        return  Result.success("成功检索", result);
    }

     /**
     * 查询出关键词所在文本
     * 组合查询
     * @param begin
     * @param end
     * @return
     */
    @RequestMapping(value = "/query")
    @ResponseBody
    public Result search(@RequestParam(name = "begin") String begin,
                         @RequestParam(name = "end") String end,
                         @RequestParam(name = "word", required = false) String word){

        if (!luceneService.isValid()) {
            return Result.success("索引数据为空");
        }
        long start;
        long last;
        try {
            start = Date.valueOf(begin).getTime();
            last = Date.valueOf(end).getTime();
        } catch (Exception e) {
            return Result.error("日期参数非法仅支持2020-05-03格式");
        }

        //searchMoreDemo里是对其他几种基本查询的使用demo
        luceneService.searchMoreDemo();

        //逻辑查询的简单demo
        SearchData result = luceneService.search(start, last, word);
        if (result == null) {
            return Result.error("检索失败");
        }
        return  Result.success("成功检索", result);
    }
}
