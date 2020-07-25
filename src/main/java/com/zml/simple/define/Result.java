package com.zml.simple.define;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 响应结果
 * @author zml
 * @date 2020/7/24
 */
public class Result {
    private int code = 200;

    private String result;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;

    public Result(int code, String result, String message, Object data) {
        this.code = code;
        this.result = result;
        this.message = message;
        this.data = data;
    }

    public Result(String result, String message, Object data) {
        this.result = result;
        this.message = message;
        this.data = data;
    }

    public Result(int code, String result, String message) {
        this.code = code;
        this.result = result;
        this.message = message;
    }

    public Result(String result, String message) {
        this.result = result;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static Result success(String messgae) {
        return new Result("SUCCESS", messgae);
    }

    public static Result success(String messgae, Object data) {
        return new Result("SUCCESS", messgae, data);
    }
    public static Result error(String messgae) {
        return new Result(417, "FAIL", messgae);
    }
}
