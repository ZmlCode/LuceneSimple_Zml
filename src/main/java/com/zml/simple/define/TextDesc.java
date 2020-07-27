package com.zml.simple.define;

import java.util.Date;

/**
 * 响应结构体关于索引内容
 * @author zml
 * @date 2020/7/25
 */
public class TextDesc {

    private String docId;

    private String createDate;

    private String fileName;

    private String content;

    public TextDesc() {
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
