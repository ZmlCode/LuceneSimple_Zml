package com.zml.simple.controller;

import com.zml.simple.define.Result;
import com.zml.simple.define.SearchData;
import com.zml.simple.service.LuceneService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 使用lucene查询语法去查询
 *   1.用QuertParser去构造查询
 *        *   后续慢慢支持以下内容：
 *      *   取出片段 高亮展示
 *      *   带出频次 并分页展示
 * @author zml
 * @date 2020/7/28
 */
@Controller
public class ParseQueryController {

    @Resource
    private LuceneService luceneService;

    /**
     * 模糊查询接口
     * @return
     */
    @RequestMapping(value = "/likeQuery/{word}")
    @ResponseBody
    public Result search(@PathVariable(name = "word") String word){
        if (!luceneService.isValid()) {
            return Result.success("索引数据为空");
        }

        //使用扩展的模糊查询
        SearchData result = luceneService.likeSearch(word);
        if (result == null) {
            return Result.error("检索失败");
        }
        return  Result.success("成功检索", result);
    }

    /**
     * 使用lucene查询语法查询
     * @return
     */
    @RequestMapping(value = "/parseQuery/{queryStr}")
    @ResponseBody
    public Result searchParser(@PathVariable(name = "queryStr") String queryStr){
        if (!luceneService.isValid()) {
            return Result.success("索引数据为空");
        }

        //这里只给出一个假的例子 和接口业务无关 学习扩展模糊搜索时练习内容
        SearchData result =  luceneService.searchQueryStr(queryStr);
        if (result == null) {
            return Result.error("检索失败");
        }
        return  Result.success("成功检索", result);
    }
}
