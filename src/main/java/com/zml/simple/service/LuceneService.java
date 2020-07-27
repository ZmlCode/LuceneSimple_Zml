package com.zml.simple.service;

import com.zml.simple.dao.LuceneDao;
import com.zml.simple.define.SearchData;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.InputStream;

import static com.zml.simple.define.IndexConstantDefine.*;

/**
 * 业务层
 * @author zml
 * @date 2020/7/24
 */
@Service
public class LuceneService {
    @Resource
    private LuceneDao luceneDao;

    /**
     * 建索
     * @param fileName
     * @param inputStream
     * @return
     */
    public boolean index(String fileName, InputStream inputStream) {
        return luceneDao.index(fileName, inputStream);
    }

    /**
     * 最简单的查询demo
     * @param word
     * @return
     */
    public SearchData search(String word) {
        return luceneDao.search(word);
    }

    /**
     * 数字范围查询 demo
     * @param start
     * @param end
     * @return
     */
    public SearchData search(long start, long end) {
        return luceneDao.search(start, end);
    }


    /**
     * 逻辑查询 demo
     * @param start
     * @param end
     * @return
     */
    public SearchData search(long start, long end, String word) {
        //逻辑查询 可以通过builder.build()可以看到逻辑查询关系
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //比如： 索引创建时间start~end 且 有关键词命中（关键词命中指的是：文本内容里有word或者标题里有word)
        if (!StringUtils.isEmpty(word)) {
            BooleanQuery.Builder wordBuilder = new BooleanQuery.Builder();
            wordBuilder.add(new TermQuery(new Term(FIELD_TITLE, word)), BooleanClause.Occur.SHOULD);
            wordBuilder.add(new TermQuery(new Term(FIELD_TEXT, word)), BooleanClause.Occur.SHOULD);

            builder.add(wordBuilder.build(), BooleanClause.Occur.MUST);
        }

        Query timeQuery = LongPoint.newRangeQuery(FIELD_INDEX_TIME, start, end);
        builder.add(timeQuery, BooleanClause.Occur.MUST);

        /**
         * MUST交集 SHOULD或集 MUST_NOT非集
         *  +a +b： a&b
         *  a b： a||b
         *  +a -b： a &！b
         */
        return luceneDao.search(builder.build());
    }

    /**
     * 常见的基本查询的使用
     *  短语查询 通配符查询 前缀查询 模糊查询
     */
    public void searchMoreDemo() {
        /**
         * 以下几种查询 数据背景举例假设：索引里有Department Data and TextContent More这样一句话 基于StandAnalyze单字分词
         */
        //短语查询   slop=2+才能查询到
        PhraseQuery.Builder pBuilder = new PhraseQuery.Builder();
        pBuilder.add(new Term(FIELD_TEXT, "department"));
        pBuilder.add(new Term(FIELD_TEXT, "textcontent"));
        pBuilder.setSlop(100);
        luceneDao.search(pBuilder.build());

        //通配符查询  可以使用* ?  查到TextContent的文档所在
        WildcardQuery wildcardQuery = new WildcardQuery(new Term(FIELD_TEXT, "*content"));
        luceneDao.search(wildcardQuery);

        //前缀查询 查到TextContent的文档所在
        PrefixQuery prefixQuery = new PrefixQuery(new Term(FIELD_TEXT, "text"));
        luceneDao.search(prefixQuery);

        //模糊查询  原理上基于编辑距离算法 0,1,2  比如这里只要满足编辑1次  textconten->textcontent后，就能出现在文档中了，值越大搜索出的文档越多
        FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(FIELD_TEXT, "textconten"),2);
        luceneDao.search(fuzzyQuery);
    }

    public boolean isValid() {
        return luceneDao.getReader() != null;
    }

}
