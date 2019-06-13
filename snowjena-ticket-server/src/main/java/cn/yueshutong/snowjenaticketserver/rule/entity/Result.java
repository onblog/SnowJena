package cn.yueshutong.snowjenaticketserver.rule.entity;

import lombok.Data;

import java.util.List;

/**
 * Create by yster@foxmail.com 2019/5/26 0026 13:48
 */
@Data
public class Result<T> {
    private int code;
    private String msg;
    private long count;
    private List<T> data;


}
