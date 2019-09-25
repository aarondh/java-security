package org.daisleyharrison.security.services.datastore.jsondb.models;

import java.lang.reflect.Field;
import java.util.List;

import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.DatastoreCursor;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.models.datastore.QueryBuilder;
import org.daisleyharrison.security.services.datastore.jsondb.DatastoreServiceJsonDb;

import io.jsondb.JsonDBTemplate;

public class DatastoreCollectionImpl<T> implements DatastoreCollection<T> {
    private DatastoreServiceJsonDb service;
    private Field idField;
    private JsonDBTemplate jsonDb;
    private Class<T> type;

    public DatastoreCollectionImpl(DatastoreServiceJsonDb service, Class<T> type, String idFieldName) {
        this.service = service;
        this.jsonDb = service.getJsonDbTemplate();
        this.type = type;
        try {
            this.idField = type.getDeclaredField(idFieldName);
            this.idField.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            throw new IllegalArgumentException("idFieldName no such field \"" + idFieldName + "\" in " + type.getName());
        }
        if (!this.jsonDb.collectionExists(type)) {
            this.jsonDb.createCollection(type);
        }
    }

    @Override
    public T findById(Object id) {
        return this.jsonDb.findById(id, this.type);
    }

    public QueryBuilder buildQuery() {
        return new QueryBuilderImpl<T>(this.type, this.idField);
    }

    @Override
    public DatastoreCursor<T> find(Query query) {
        List<T> dataset = this.jsonDb.find(query.toString(), this.type);
        return new DatastoreCursorImpl<T>(this, dataset.stream());
    }

    @Override
    public DatastoreCursor<T> find() {
        List<T> dataset = this.jsonDb.findAll(this.type);
        return new DatastoreCursorImpl<T>(this, dataset.stream());
    }

    @Override
    public long count() {
        return find().count();
    }

    @Override
    public long count(Query query) {
        List<T> dataset = this.jsonDb.find(query.toString(), this.type);
        return dataset.size();
    }

    @Override
    public boolean remove(T model) {
        return this.jsonDb.remove(model, this.type) != null;
    }

    @Override
    public void insert(T model) {
        this.jsonDb.insert(model);
    }

    @Override
    public void save(T model) {
        this.jsonDb.save(model, this.type);
    }

    @Override
    public void upsert(T model) {
        this.jsonDb.upsert(model);
    }

    @Override
    public void close() throws Exception {

    }

}