package cn.yueshutong.aspectj.annotations;

import cn.yueshutong.enums.AcquireModel;
import cn.yueshutong.enums.Algorithm;
import cn.yueshutong.enums.LimiterModel;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Limiter {
    String id() default "";

    double QPS() default 20;

    long initialDelay() default 0;

    AcquireModel acquireModel() default AcquireModel.FAILFAST;

    Algorithm algorithm() default Algorithm.TOKENBUCKET;

    LimiterModel currentModel() default LimiterModel.POINT;

}