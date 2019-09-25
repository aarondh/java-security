package org.daisleyharrison.security.services.datastore.mongodb.models;

import java.lang.reflect.Field;

import com.mongodb.WriteResult;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.DatastoreCursor;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.models.datastore.QueryBuilder;
import org.daisleyharrison.security.services.datastore.mongodb.DatastoreServiceMongoDb;

import org.jongo.FindOne;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.Oid;

public class DatastoreCollectionImpl<T> implements DatastoreCollection<T> {
    private DatastoreServiceMongoDb service;
    private MongoCollection collection;
    private Field mongoObjectIdField;
    private Field userIdField;
    private Class<T> type;

    public DatastoreCollectionImpl(DatastoreServiceMongoDb service, Class<T> type, String idFieldName) {
        this.service = service;
        this.type = type;
        this.collection = service.getJongo().getCollection(type.getName());
        try {
            this.mongoObjectIdField = type.getDeclaredField(this.collection.MONGO_DOCUMENT_ID_NAME);
            this.mongoObjectIdField.setAccessible(true);
        } catch (NoSuchFieldException exception) {
        }
        if (idFieldName != null) {
            try {
                this.userIdField = type.getDeclaredField(idFieldName);
                this.userIdField.setAccessible(true);
                String keys = String.format("{_id: 1, %s: 1}", idFieldName);
                this.collection.ensureIndex(keys);
            } catch (NoSuchFieldException exception) {
            }
        }
    }

    public String getObjectId(T model) {
        try {
            Object id = this.mongoObjectIdField.get(model);
            return id == null ? null : Oid.withOid(id.toString());
        } catch (IllegalAccessException exception) {
            return null;
        }
    }

    @Override
    public long count() {
        return collection.count();
    }

    @Override
    public long count(Query query) {
        return collection.count(query.toString());
    }

    @Override
    public T findById(Object id) {
        return collection.findOne(Oid.withOid(id.toString())).as(type);
    }

    public QueryBuilder buildQuery() {
        return new QueryBuilderImpl(this.type);
    }

    @Override
    public DatastoreCursor<T> find(Query query) {
        MongoCursor<T> cursor = collection.find(query.toString()).as(this.type);
        return new DatastoreCursorImpl<T>(this, cursor);
    }

    @Override
    public DatastoreCursor<T> find() {
        MongoCursor<T> cursor = collection.find().as(this.type);
        return new DatastoreCursorImpl<T>(this, cursor);
    }

    @Override
    public boolean remove(T model) {
        WriteResult writeResult = this.collection.remove(getObjectId(model));
        return writeResult.wasAcknowledged();
    }

    private void ensureObjectIdWasRetrieved(T model, WriteResult writeResult) {
        if (this.mongoObjectIdField != null) {
            try {
                Object objectId = this.mongoObjectIdField.get(model);
                if (objectId != null) {
                    return; // i d populated
                }
                objectId = writeResult.getUpsertedId(); // try to get the id from the WriteResult
                if (objectId != null) {
                    this.mongoObjectIdField.setAccessible(true);
                    this.mongoObjectIdField.set(model, objectId);
                    return;
                }
            } catch (IllegalAccessException ex) {

            }
        }
        if (this.userIdField != null && this.mongoObjectIdField != null
                && this.userIdField != this.mongoObjectIdField) {
            // Try to get the object id by querying the user id field
            try {
                String query = String.format("{%s: \"%s\"}", this.userIdField.getName(),
                        this.userIdField.get(model).toString());
                FindOne findOne = this.collection.findOne(query);
                T inserted = findOne.as(type);
                this.mongoObjectIdField.set(model, this.mongoObjectIdField.get(inserted));
            } catch (IllegalAccessException ex) {

            }
        }
    }

    @Override
    public void insert(T model) {
        WriteResult writeResult = this.collection.insert(model);
        ensureObjectIdWasRetrieved(model, writeResult);
    }

    @Override
    public void save(T model) {
        this.collection.save(model);
    }

    @Override
    public void upsert(T model) {
        if (this.mongoObjectIdField == null) {
            throw new IllegalStateException(
                    "upsert cannot be done. The type" + type.getSimpleName() + " does not declare the _id field.");
        }
        if (this.collection.findOne(getObjectId(model)) == null) {
            save(model);
        } else {
            insert(model);
        }
    }

    @Override
    public void close() throws Exception {
    }

}