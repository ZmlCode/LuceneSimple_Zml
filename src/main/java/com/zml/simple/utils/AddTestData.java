package com.zml.simple.utils;

import com.zml.simple.service.LuceneService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;

/**
 * 程序运行时检索测试数据
 * @author zml
 * @date 2020/7/28
 */
@Component
public class AddTestData{
    private Logger log = LogManager.getLogger(AddTestData.class);

    @Resource
    private LuceneService luceneService;

    @Value("${data.FilePath}")
    private File testDataFile;

    @PostConstruct
    public void addTestData() {
        if (!testDataFile.exists()) {
            log.info(String.format("test data path not exist", testDataFile.getAbsoluteFile()));
            return;
        }
        //构造基于单字分词的索引
        for (File f : testDataFile.listFiles()) {
            try {
                luceneService.index(f.getName(),new FileInputStream(f.getPath()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
