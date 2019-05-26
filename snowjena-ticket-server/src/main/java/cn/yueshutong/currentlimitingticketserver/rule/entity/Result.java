package cn.yueshutong.currentlimitingticketserver.rule.entity;

import cn.yueshutong.commoon.entity.LimiterRule;
import lombok.Data;

import java.util.List;

/**
 * Create by yster@foxmail.com 2019/5/26 0026 13:48
 */
@Data
public class Result {
    private int code;
    private String msg;
    private long count;
    private List<LimiterRule> data;

}
