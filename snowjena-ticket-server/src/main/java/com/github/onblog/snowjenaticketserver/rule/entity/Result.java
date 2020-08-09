package com.github.onblog.snowjenaticketserver.rule.entity;

import com.github.onblog.snowjenaticketserver.exception.ResultEnum;

import java.util.List;

/**
 * Create by yster@foxmail.com 2019/5/26 0026 13:48
 */
public class Result<T> {
    private int code;
    private String msg;
    private long count;
    private List<T> data;

    public Result() {
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(ResultEnum success) {
        this.code = success.getCode();
        this.msg = success.getMsg();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
