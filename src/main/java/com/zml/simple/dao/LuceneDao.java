package com.zml.simple.dao;

import com.zml.simple.define.SearchData;
import com.zml.simple.define.TextDesc;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import static com.zml.simple.define.IndexConstantDefine.*;

/**
 * 基于lucene的CRUD
 * @author zml
 * @date 2020/7/24
 */
@Component
public class LuceneDao {
    @Value("${index.FilePath}")
    private File indexFile;

    private IndexReader reader;

    private Directory directory;

    /**
     * 初始化在大数据量时耗时较长
     *    放在类加载时处理
     */
    @PostConstruct
    private void init() {
        if (!indexFile.exists()) {
            indexFile.mkdirs();
        }
        try {
            //加载检索的索引
            directory = FSDirectory.open(indexFile.toPath());
            if (indexFile.listFiles().length != 0) {
                reader = DirectoryReader.open(directory);
            }
            //后续关注一下啊talent的reader是在什么时候加载 当索引内容空时怎么处理的
            //程序启动时如果加载索引文件错误时 怎么办
            //还需要关注一下 每次写完文档后，IndexReader在使用时加载耗时怎么样
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public boolean index(String fileName, InputStream inputStream) {
        try {
            //读取文件获取出内容字符流
            InputStreamReader fileReader = new InputStreamReader(inputStream);

            //使用最基本的 单字分词器 构造一个IndexWriter
            IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));

            //构造文档域  标题 索引创建时间 文本内容
            Document doc = new Document();
            doc.add(new StringField(FIELD_TITLE, fileName, Field.Store.YES));

          /*//test: lucene不允许建立不索引不存储的内容 无意义
            FieldType type = new FieldType();
            type.setTokenized(false);
            type.setIndexOptions(IndexOptions.NONE);
            type.setStored(true);
            doc.add(new StoredField("a", "a", type));*/
            Date now = new Date();
            doc.add(new LongPoint(FIELD_INDEX_TIME, now.getTime()));
            doc.add(new StringField(FIELD_CREATE_TIME, SimpleDateFormat.getDateTimeInstance().format(now), Field.Store.YES));
            doc.add(new TextField(FIELD_TEXT, fileReader));

            //文档写成索引
            writer.addDocument(doc);
            //写完文件必须close资源 不然索引写不完，会有问题，索引文件格式错误
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public IndexReader getReader() {
        if (reader == null) {
            init();
        }
        return reader;
    }

    /**
     * 精确的按词查询
     *   最简单的整体完整例子 就不复用拆分的共用代码 作为最简单demo
     * @param word
     * @return
     */
    public SearchData search(String word) {
        SearchData result = new SearchData();
        try {
            reader = DirectoryReader.open(directory);
            //构造查询条件
            Query query = new TermQuery(new Term(FIELD_TEXT, word));
            //实例化搜索器
            IndexSearcher searcher = new IndexSearcher(reader);
            //按查询条件 查出前N条记录
            TopDocs topDocs = searcher.search(query, 10);

            //符合查询条件的总条数
            long count = topDocs.totalHits.value;
            result.setCount(count);
            List<TextDesc> descs = new ArrayList<>();
            for (ScoreDoc doc : topDocs.scoreDocs) {
                TextDesc desc = new TextDesc();;
                desc.setDocId(String.valueOf(doc.doc));
                descs.add(desc);
            }
            result.setTextDescs(descs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private TopDocs search(Query query) {
        try {
            reader = DirectoryReader.open(directory);
            //实例化搜索器
            IndexSearcher searcher = new IndexSearcher(reader);
            //按查询条件 查出前N条记录
            TopDocs topDocs = searcher.search(query, 10);
            return topDocs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 数字范围查询
     * @param start
     * @param end
     * @return
     */
    public SearchData search(long start, long end) {
        SearchData result = new SearchData();
        try {
            reader = DirectoryReader.open(directory);
            //构造查询条件   NumericRangeQuery对数字范围的查询在lucene7以上废弃 替换使用LongPoint.newRangeQuery
            Query query = LongPoint.newRangeQuery(FIELD_INDEX_TIME, start, end);
            //实例化搜索器
            IndexSearcher searcher = new IndexSearcher(reader);
            //按查询条件 查出前N条记录
            TopDocs topDocs = searcher.search(query, 10);

            //符合查询条件的总条数
            long count = topDocs.totalHits.value;
            result.setCount(count);
            List<TextDesc> descs = new ArrayList<>();
            for (ScoreDoc doc : topDocs.scoreDocs) {
                //此处这是一个正排查询
                Document document = searcher.doc(doc.doc);
                TextDesc desc = new TextDesc();;
                desc.setDocId(String.valueOf(doc.doc));
                //createDdate要查出来 必须要求建索时存储
                //如果使用document.get(FIELD_INDEX_TIME)，=null 是因为建索时没存储
                desc.setCreateDate(document.get(FIELD_CREATE_TIME));
                descs.add(desc);
            }
            result.setTextDescs(descs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
