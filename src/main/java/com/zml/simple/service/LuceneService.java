package com.zml.simple.service;

import com.zml.simple.dao.LuceneDao;
import com.zml.simple.define.SearchData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.QueryBuilder;
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
     *   查询逻辑基于lucene查询语法
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
        //短语查询不好的原因：必须保证和内容顺序一致，倒序时查不到
        PhraseQuery.Builder pBuilder2 = new PhraseQuery.Builder();
        pBuilder2.add(new Term(FIELD_TEXT, "池"));
        pBuilder2.add(new Term(FIELD_TEXT, "刘"));
        pBuilder2.setSlop(3);
        luceneDao.search(pBuilder.build());

        //跨度查询不好的原因：2个SpanTerm以上时，slop必须从最开始字符到最后字符总的slop才命中 slop不能用于控制左右两个词的单独间距
        SpanTermQuery night = new SpanTermQuery(new Term(FIELD_TEXT,"刘"));
        SpanTermQuery him = new SpanTermQuery(new Term(FIELD_TEXT,"池"));
        SpanTermQuery clothes = new SpanTermQuery(new Term(FIELD_TEXT,"金"));
        SpanQuery[] night_him_clothes=new SpanQuery[]{night,him,clothes};
        /*SpanQuery[] night_him_clothes=new SpanQuery[]{night,him};*/
        SpanNearQuery query=new SpanNearQuery(night_him_clothes, 4,false);
        luceneDao.search(query);

        //通配符查询  可以使用* ?  查到TextContent的文档所在
        WildcardQuery wildcardQuery = new WildcardQuery(new Term(FIELD_TEXT, "*content"));
        luceneDao.search(wildcardQuery);

        //前缀查询 查到TextContent的文档所在
        PrefixQuery prefixQuery = new PrefixQuery(new Term(FIELD_TEXT, "text"));
        luceneDao.search(prefixQuery);

        //模糊查询  原理上基于编辑距离算法 0,1,2  比如这里只要满足编辑1次  textconten->textcontent后，就能出现在文档中了，值越大搜索出的文档越多
        FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(FIELD_TEXT, "textconten"),2);
        luceneDao.search(fuzzyQuery);

        //queryBulid可以设置create的各种查询器
        QueryBuilder builder = new QueryBuilder(new StandardAnalyzer());
    }

    public boolean isValid() {
        return luceneDao.getReader() != null;
    }

    /**
     * QureyParser的使用
     * @param word
     * @return
     */
    public SearchData likeSearch(String word) {
        //lucene查询语法的继续使用
        try {
            QueryParser fieldParser = new QueryParser(FIELD_TEXT, new StandardAnalyzer());
            luceneDao.search(fieldParser.parse("姜熙健")); //text:姜 text:熙 text:健 也就是 姜||熙 ||健

            QueryParser parser = new QueryParser("", new StandardAnalyzer());
            Query query = parser.parse("text:java text: 光洙 AND title:简*");//java || (光 && 题目中有简字)
            luceneDao.search(query);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //自定义扩展QueryParser的简单实现
        Query fuzzyQuery = new CustomParser(FIELD_TEXT, new StandardAnalyzer()).getFuzzyQuery(FIELD_TEXT, word);
        return luceneDao.search(fuzzyQuery);
    }

    /**
     * QueryParser.parser的使用
     * lucene查询语法的继续使用demo
     * @return
     */
    public SearchData searchQueryStr(String queryStr) {
        try {
            QueryParser fieldParser = new QueryParser(FIELD_TEXT, new StandardAnalyzer());
            luceneDao.search(fieldParser.parse("姜熙健")); //text:姜 text:熙 text:健 也就是 姜||熙 ||健

            QueryParser parser = new QueryParser("", new StandardAnalyzer());
            Query query = parser.parse("text:java text: 光洙 AND title:简*");//java || (光 && 题目中有简字)
            luceneDao.search(query);

            return luceneDao.search(parser.parse(queryStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
