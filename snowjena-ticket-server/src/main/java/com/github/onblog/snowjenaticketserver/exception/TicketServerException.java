package com.github.onblog.snowjenaticketserver.exception;

public class TicketServerException extends RuntimeException {
    private Integer code;

    public TicketServerException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public TicketServerException(ResultEnum resultEnum, String message) {
        super(message);
        this.code = code;
    }

    public TicketServerException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}