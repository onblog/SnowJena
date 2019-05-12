package cn.yueshutong.exception;

public class RuleNotFound extends RuntimeException {
    public RuleNotFound(String message){
        super(message);
    }
}
