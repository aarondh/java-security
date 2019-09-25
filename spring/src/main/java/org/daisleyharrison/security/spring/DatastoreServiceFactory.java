package org.daisleyharrison.security.spring;

import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;


import org.springframework.beans.factory.annotation.Autowired;

import org.daisleyharrison.security.common.spi.KeyServiceProvider;
import org.daisleyharrison.security.services.datastore.mongodb.DatastoreServiceMongoDb;

public class DatastoreServiceFactory extends SecurityServiceFactory<DatastoreServiceProvider, DatastoreServiceMongoDb> {

    @Autowired
    private KeyServiceProvider keyService;

    public DatastoreServiceFactory() {
        super(DatastoreServiceProvider.class, DatastoreServiceMongoDb.class);
    }

}