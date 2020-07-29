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
    //存储基于最简单的单字分词方式建索和查询的索引路径
    @Value("${index.stand.FilePath}")
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

    public IndexReader getReader() {
        if (reader == null) {
            init();
        }
        return reader;
    }

    /**
     * 建索
     *   基于单字分词
     * @param fileName
     * @param inputStream
     * @return
     */
    public boolean index(String fileName, InputStream inputStream) {
        try {
            //读取文件获取出内容字符流
            InputStreamReader fileReader = new InputStreamReader(inputStream, "UTF-8");

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

    /**
     * 删除索引demo
     * @return
     */
    public boolean deleteIndex() {
        try {
            IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));
            //指定删除符合某个条件的索引
            writer.deleteDocuments(new Term(FIELD_TITLE,"test.txt"));
            //writer.deleteAll();删除全部 危险动作
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 更新索引demo
     * @return
     */
    public boolean updateIndex() {
        try {
            IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));

            //这里构造要更新成的测试数据
            Document doc = new Document();
            doc.add(new StringField(FIELD_TITLE, "updatedNew.txt", Field.Store.YES));
            Date now = new Date();
            doc.add(new LongPoint(FIELD_INDEX_TIME, now.getTime()));
            doc.add(new StringField(FIELD_CREATE_TIME, SimpleDateFormat.getDateTimeInstance().format(now), Field.Store.YES));
            doc.add(new TextField(FIELD_TEXT, "test data test data more!", Field.Store.YES));

            //原理：按条件查找，找到后删除然后覆盖新的doc;如果没找到直接插入新的doc.
            writer.updateDocument(new Term(FIELD_TITLE,"test.txt"), doc);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
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

    private TopDocs searchTopDocs(Query query) {
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
     * 组合查询
     *   要注意的是，搜索的域如果整体建索的话比如StringField,
     *       需要整体搜索，不能单独使用里面的词，因为建索时没分词
     * @return
     */
    public SearchData search(Query query) {
        SearchData result = new SearchData();
        try {
            reader = DirectoryReader.open(directory);
            //实例化搜索器
            IndexSearcher searcher = new IndexSearcher(reader);
            //按查询条件 查出前N条记录
            TopDocs topDocs = searchTopDocs(query);

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
                desc.setFileName(document.get(FIELD_TITLE));
                descs.add(desc);
            }
            result.setTextDescs(descs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
