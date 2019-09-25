package org.daisleyharrison.security.samples.spring.microservices.shared.security;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.daisleyharrison.security.common.spi.PopTokenConsumer;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;

public class JweSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private TokenizerServiceProvider tokenizerService;

    private PopTokenConsumer popTokenConsumer;
    
    private JweAuthorizationProvider jweAuthorizationProvider;

    public JweSecurityConfigurer(TokenizerServiceProvider tokenizerService, PopTokenConsumer popTokenConsumer, JweAuthorizationProvider jweAuthorizationProvider) {
        this.tokenizerService = tokenizerService;
        this.popTokenConsumer = popTokenConsumer;
        this.jweAuthorizationProvider = jweAuthorizationProvider;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        JweTokenAuthenticationFilter customFilter = new JweTokenAuthenticationFilter(tokenizerService, popTokenConsumer, jweAuthorizationProvider);
        http.exceptionHandling()
        .authenticationEntryPoint(new JweAuthenticationEntryPoint())
        .and()
        .addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}