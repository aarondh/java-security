package org.daisleyharrison.security.spring;

import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.spi.TokenizerServiceProvider;
import org.daisleyharrison.security.services.tokenizer.TokenizerService;

import org.springframework.beans.factory.annotation.Autowired;

public class TokenizerServiceFactory extends SecurityServiceFactory<TokenizerServiceProvider, TokenizerService> {

    @Autowired
    DatastoreServiceProvider datastoreService;
    
    public TokenizerServiceFactory() {
        super(TokenizerServiceProvider.class, TokenizerService.class);
    }

}