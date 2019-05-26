package cn.yueshutong.core.exception;

public class CurrentLimitingException extends RuntimeException {

    public CurrentLimitingException(String have_current_limiting) {
        super(have_current_limiting);
    }

}
