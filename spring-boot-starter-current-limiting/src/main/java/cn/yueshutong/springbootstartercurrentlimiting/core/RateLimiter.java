package cn.yueshutong.springbootstartercurrentlimiting.core;

public class RateLimiter {
    private static RateLimiter rateLimiter = new RateLimiter();;

    public static RateLimiter create(int num){
        return rateLimiter;
    }



}
