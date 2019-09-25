package org.daisleyharrison.security.common.spi;

import org.daisleyharrison.security.common.exceptions.DatastoreException;
import org.daisleyharrison.security.common.models.cypher.StringCypher;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;

public interface DatastoreServiceProvider extends SecurityServiceProvider {
    public void configure();
    public void setCypher(StringCypher cypher);
    public <T> DatastoreCollection<T> openCollection(Class<T> type, String idFieldName) throws DatastoreException;
}