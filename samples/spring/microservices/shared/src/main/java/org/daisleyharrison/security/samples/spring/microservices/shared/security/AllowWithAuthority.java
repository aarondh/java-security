package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Allow access to request if the user has a give authority
 *
 * @author Aaron G Daisley-Harrison
 *
 */

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowWithAuthority {
    public String[] value();
}