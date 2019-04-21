package cn.yueshutong.springbootstartercurrentlimiting.common;

public enum CurrentEnum {
    MESSAGE("<pre>The specified service is not currently available.</pre>");

    private String message;

    CurrentEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
