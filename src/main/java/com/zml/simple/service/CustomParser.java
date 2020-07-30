package com.zml.simple.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.CharStream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserTokenManager;
import org.apache.lucene.search.Query;


/**
 * 扩展QueryParser
 *  需要说明 QueryParser.parser的解析lucene语法的
 * @author zml
 * @date 2020/7/29
 */
public class CustomParser extends QueryParser {


    public CustomParser(String f, Analyzer a) {
        super(f, a);
    }

    public CustomParser(CharStream stream) {
        super(stream);
    }

    public CustomParser(QueryParserTokenManager tm) {
        super(tm);
    }

    @Override
    protected Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) throws ParseException {
        return super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
    }

    /**
     * 通配符查询禁止使用
     * @param field
     * @param termStr
     * @return
     * @throws ParseException
     */
    @Override
    protected Query getWildcardQuery(String field, String termStr) throws ParseException {
        throw new ParseException("本项目不支持禁用通配符查询");
    }

    /**
     * 模糊查询扩展
     *    cny->rmb的转化  本例只限制输入cny也能查出来rmb。
     * @param field
     * @param termStr
     * @return
     * @throws ParseException
     */
    public Query getFuzzyQuery(String field, String termStr)  {
        termStr = termStr.toLowerCase().replace("cny", "rmb");
        try {
            //minSimlarity 为达到一样最多修正位数  最大值目前是2
            return super.getFuzzyQuery(field, termStr, 2.0F);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
