package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.daisleyharrison.security.spring.SecurityServiceConfiguration;

import org.springframework.context.annotation.Import;

/**
 * Annotation to activate Eureka Server related configuration.
 * {@link EurekaServerAutoConfiguration}
 *
 * @author Dave Syer
 * @author Biju Kunjummen
 *
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ SecurityServiceConfiguration.class, SecurityConfig.class })
public @interface EnableDHSecurity {

}