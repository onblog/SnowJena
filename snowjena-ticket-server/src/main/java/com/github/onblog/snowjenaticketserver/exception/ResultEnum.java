package com.github.onblog.snowjenaticketserver.exception;

public enum ResultEnum {
    ERROR(-1, "错误"),
    SUCCESS(0, "成功"),
    ;

    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
