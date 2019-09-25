package org.daisleyharrison.security.samples.spring.webtalker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface WebTalkerParameter{
    String value() default "";
    boolean queryParam() default false;
    boolean pathVariable() default false;
    boolean header() default false;
}