package cn.yueshutong.springbootstartercurrentlimiting.method.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface CurrentLimiter {

    double QPS() default 20;

    long initialDelay() default 0;

    boolean failFast() default true;

    boolean overflow() default false;
}
