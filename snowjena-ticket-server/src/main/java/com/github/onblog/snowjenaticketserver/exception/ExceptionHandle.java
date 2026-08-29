package com.github.onblog.snowjenaticketserver.exception;

import com.github.onblog.snowjenaticketserver.rule.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(annotations = ResultException.class)
public class ExceptionHandle {
    //记录日志
    private final static Logger logger = LoggerFactory.getLogger(TicketServerException.class);

    /**
     * 捕获异常 封装返回数据
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result<?> handle(Exception e) {
        if (e instanceof TicketServerException) {
            return new Result(((TicketServerException) e).getCode(), e.getMessage());
        } else {
            logger.error("[系统异常] {}", e.getMessage());
            return new Result(ResultEnum.ERROR.getCode(), e.getMessage());
        }
    }
}
