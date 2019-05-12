package cn.yueshutong.exception;

public class RuleNotParameter extends RuntimeException {
    public RuleNotParameter(String message){
        super(message);
    }
}