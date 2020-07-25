package com.zml.simple.service;

import com.zml.simple.dao.LuceneDao;
import com.zml.simple.define.SearchData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * 业务层
 * @author zml
 * @date 2020/7/24
 */
@Service
public class LuceneService {
    @Resource
    private LuceneDao luceneDao;

    public boolean index(String fileName, InputStream inputStream) {
        return luceneDao.index(fileName, inputStream);
    }

    public SearchData search(String word) {
        return luceneDao.search(word);
    }

    public SearchData search(long start, long end) {
        return luceneDao.search(start, end);
    }

    public boolean isValid() {
        return luceneDao.getReader() != null;
    }
}
