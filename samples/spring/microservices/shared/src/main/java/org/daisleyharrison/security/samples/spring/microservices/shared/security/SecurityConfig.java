package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import org.daisleyharrison.security.common.spi.PopTokenConsumer;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.samples.spring.microservices.shared.security.JweSecurityConfigurer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private Collection<Object> controllerBeans;

    public SecurityConfig(ApplicationContext ctx) {
        Map<String, Object> controllers = ctx.getBeansWithAnnotation(Controller.class);
        this.controllerBeans = controllers.values();
    }

    @Autowired
    TokenizerServiceProvider tokenizerService;

    @Autowired
    PopTokenConsumer popTokenConsumer;

    @Autowired
    JweAuthorizationProvider jweAuthorizationProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    private List<String> getPatterns(Method method) {
        List<String> patterns = new ArrayList<>();
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            patterns.addAll(Arrays.asList(requestMapping.value()));
        }
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            patterns.addAll(Arrays.asList(getMapping.value()));
        }
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            patterns.addAll(Arrays.asList(postMapping.value()));
        }
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            patterns.addAll(Arrays.asList(putMapping.value()));
        }
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            patterns.addAll(Arrays.asList(deleteMapping.value()));
        }
        return patterns;
    }

    private HttpMethod toHttpMethod(RequestMethod requestMethod) {
        switch (requestMethod) {
        default:
        case GET:
            return HttpMethod.GET;
        case POST:
            return HttpMethod.POST;
        case PUT:
            return HttpMethod.PUT;
        case PATCH:
            return HttpMethod.PATCH;
        case DELETE:
            return HttpMethod.DELETE;
        case OPTIONS:
            return HttpMethod.OPTIONS;
        case HEAD:
            return HttpMethod.HEAD;
        case TRACE:
            return HttpMethod.TRACE;
        }
    }

    private List<HttpMethod> getRequestMethods(Method method) {
        List<HttpMethod> httpMethods = new ArrayList<>();
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            for (RequestMethod requestMethod : requestMapping.method()) {
                httpMethods.add(toHttpMethod(requestMethod));
            }
        }
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            httpMethods.add(HttpMethod.GET);
        }
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            httpMethods.add(HttpMethod.POST);
        }
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            httpMethods.add(HttpMethod.PUT);
        }
        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        if (patchMapping != null) {
            httpMethods.add(HttpMethod.PATCH);
        }
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            httpMethods.add(HttpMethod.DELETE);
        }
        return httpMethods;
    }

    private String rootContextPath = "/";

    private String[] prependControllerPatterns(List<String> methodPatterns, String[] controllerPatterns) {
        if (methodPatterns == null) {
            return new String[0];
        }
        List<String> patterns = new ArrayList<>();
        if (controllerPatterns == null || controllerPatterns.length == 0) {
            for (String methodPattern : methodPatterns) {
                String pattern = rootContextPath + methodPattern;
                patterns.add(pattern);
            }
        }
        for (String methodPattern : methodPatterns) {
            for (String controllerPattern : controllerPatterns) {
                String pattern = rootContextPath + controllerPattern + methodPattern;
                patterns.add(pattern);
            }
        }
        return patterns.toArray(String[]::new);
    }

    private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeAllowAnonymousRequests(
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry intercept) {

        for (Object controllerBean : controllerBeans) {
            Class<?> beanType = controllerBean.getClass();
            RequestMapping requestMappingOnController = beanType.getAnnotation(RequestMapping.class);
            for (Method method : beanType.getMethods()) {
                if (!method.isAnnotationPresent(AllowWithAuthority.class)
                        && !method.isAnnotationPresent(AllowWithRole.class)) {
                    String[] methodPatterns = prependControllerPatterns(getPatterns(method),
                            requestMappingOnController == null ? null : requestMappingOnController.value());
                    if (methodPatterns != null) {
                        for (HttpMethod httpMethod : getRequestMethods(method)) {
                            StringBuilder message = new StringBuilder();
                            message.append(httpMethod);
                            message.append(" access for anonymous");
                            message.append(" granted for pattern(s): ");
                            message.append(String.join(", ", methodPatterns));
                            intercept = intercept.antMatchers(httpMethod, methodPatterns).permitAll();
                            LOGGER.info(message.toString());
                        }
                    }
                }
            }
        }
        return intercept;
    }

    private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeAllowWithRoleRequests(
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry intercept) {
        for (Object controllerBean : controllerBeans) {
            Class<?> beanType = controllerBean.getClass();
            AllowWithRole allowWithRoleOnController = null;
            RequestMapping requestMappingOnController = beanType.getAnnotation(RequestMapping.class);
            if (beanType.isAnnotationPresent(AllowWithRole.class)) {
                allowWithRoleOnController = beanType.getAnnotation(AllowWithRole.class);
            }
            for (Method method : beanType.getMethods()) {
                String[] patterns = prependControllerPatterns(getPatterns(method),
                        requestMappingOnController == null ? null : requestMappingOnController.value());
                if (patterns != null) {
                    AllowWithRole allowWithRole;
                    if (method.isAnnotationPresent(AllowWithRole.class)) {
                        allowWithRole = method.getAnnotation(AllowWithRole.class);
                    } else {
                        allowWithRole = allowWithRoleOnController;
                    }
                    if (allowWithRole != null) {
                        for (HttpMethod httpMethod : getRequestMethods(method)) {
                            StringBuilder message = new StringBuilder();
                            message.append(httpMethod);
                            message.append(" access for role(s) ");
                            message.append(String.join(", ", allowWithRole.value()));
                            message.append(" granted for pattern(s): ");
                            message.append(String.join(", ", patterns));
                            intercept = intercept.antMatchers(patterns).hasAnyRole(allowWithRole.value());
                            LOGGER.info(message.toString());
                        }
                    } // if rolesAllowed are defined for this method or on the controller
                } // if method has request mapping
            }
        }
        return intercept;
    }

    private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeAllowWithAuthorityRequests(
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry intercept) {
        for (Object controllerBean : controllerBeans) {
            Class<?> beanType = controllerBean.getClass();
            AllowWithAuthority allowWithAuthorityOnController = beanType.getAnnotation(AllowWithAuthority.class);
            RequestMapping requestMappingOnController = beanType.getAnnotation(RequestMapping.class);
            for (Method method : beanType.getMethods()) {
                String[] patterns = prependControllerPatterns(getPatterns(method),
                        requestMappingOnController == null ? null : requestMappingOnController.value());
                if (patterns != null) {
                    AllowWithAuthority allowWithAuthority;
                    if (method.isAnnotationPresent(AllowWithAuthority.class)) {
                        allowWithAuthority = method.getAnnotation(AllowWithAuthority.class);
                    } else {
                        allowWithAuthority = allowWithAuthorityOnController;
                    }
                    if (allowWithAuthority != null) {
                        for (HttpMethod httpMethod : getRequestMethods(method)) {
                            StringBuilder message = new StringBuilder();
                            message.append(httpMethod);
                            message.append(" access for authorities ");
                            message.append(String.join(", ", allowWithAuthority.value()));
                            message.append(" granted for pattern(s): ");
                            message.append(String.join(", ", patterns));
                            intercept = intercept.antMatchers(httpMethod, patterns)
                                    .hasAnyAuthority(allowWithAuthority.value());
                            LOGGER.info(message.toString());
                        }
                    } // if rolesAllowed are defined for this method or on the controller
                } // if method has request mapping
            }
        }
        return intercept;
    }

    private ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequests(
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry intercept) {
        intercept = authorizeAllowAnonymousRequests(intercept);
        intercept = authorizeAllowWithRoleRequests(intercept);
        intercept = authorizeAllowWithAuthorityRequests(intercept);

        return intercept.anyRequest().authenticated();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http = http
            .httpBasic().disable()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .anonymous().and();
        authorizeRequests(http.authorizeRequests())
            .and()
            .apply(new JweSecurityConfigurer(tokenizerService, popTokenConsumer, jweAuthorizationProvider));
        //@formatter:on
    }
}
