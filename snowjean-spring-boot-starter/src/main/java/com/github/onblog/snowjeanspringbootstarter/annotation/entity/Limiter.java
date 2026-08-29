package com.github.onblog.snowjeanspringbootstarter.annotation.entity;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Limiter {

    /**
     * RateLimiter id
     */
    String value() default "";

    /**
     * Call this function after denying service
     */
    String fallback() default "";

}
