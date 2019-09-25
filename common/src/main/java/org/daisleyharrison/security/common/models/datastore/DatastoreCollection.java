package org.daisleyharrison.security.common.models.datastore;

public interface DatastoreCollection<T> extends AutoCloseable {
    public T findById(Object id);
    public DatastoreCursor<T> find(Query query);
    public DatastoreCursor<T> find();
    public long count(Query query);
    public long count();
    public boolean remove(T model);
    public void insert(T model);
    public void save(T model);
    public void upsert(T model);
    public QueryBuilder buildQuery();
}