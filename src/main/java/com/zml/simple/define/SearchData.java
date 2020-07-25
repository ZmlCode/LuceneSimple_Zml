package com.zml.simple.define;

import java.util.List;

/**
 * 响应结构体
 * @author zml
 * @date 2020/7/25
 */
public class SearchData {
    private long count;

    private List<TextDesc> textDescs;

    public SearchData() {
    }

    public SearchData(int count, List<TextDesc> textDescs) {
        this.count = count;
        this.textDescs = textDescs;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<TextDesc> getTextDescs() {
        return textDescs;
    }

    public void setTextDescs(List<TextDesc> textDescs) {
        this.textDescs = textDescs;
    }
}
