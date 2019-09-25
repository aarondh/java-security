package org.daisleyharrison.security.spring;

import org.daisleyharrison.security.common.spi.CypherServiceProvider;
import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.services.cypher.CypherService;

import org.springframework.beans.factory.annotation.Autowired;

public class CypherServiceFactory extends SecurityServiceFactory<CypherServiceProvider, CypherService> {

    @Autowired
    private KeyServiceProvider keyService;

    public CypherServiceFactory() {
        super(CypherServiceProvider.class, CypherService.class);
    }

}