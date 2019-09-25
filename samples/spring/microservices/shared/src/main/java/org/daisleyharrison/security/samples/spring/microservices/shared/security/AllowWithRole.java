package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Annotation to allow access to request if the user has a given role
 *
 * @author Aaron G Daisley-Harrison
 *
 */

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowWithRole {
    public String[] value();
}