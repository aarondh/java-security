package org.daisleyharrison.security.samples.jerseyService.filters;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * When a resource is marked with this annotation the user will be redirected to
 * the path specified by the AuthenticationFilter, if access to the resource was
 * denied
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface NonceRequired {

}
