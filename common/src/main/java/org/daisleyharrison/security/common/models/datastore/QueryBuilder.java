package org.daisleyharrison.security.common.models.datastore;

public interface QueryBuilder {
    QueryBuilder is(Object value);
    QueryBuilder isNot(Object value);
    QueryBuilder lessThan(Object value);
    QueryBuilder lessOrEqual(Object value);
    QueryBuilder greaterThan(Object value);
    QueryBuilder greaterOrEqual(Object value);
    QueryBuilder contains(String value);
    QueryBuilder matches(String value);
    QueryBuilder startsWith(String value);
    QueryBuilder endsWith(String value);
    QueryBuilder in(Object... values);
    QueryBuilder notIn(Object... values);
    QueryBuilder isNull();
    QueryBuilder isNotNull();
    QueryBuilder and(String propertyName);
    QueryBuilder or(String propertyName);
    QueryBuilder and();
    QueryBuilder or();
    QueryBuilder property(String propertyName);
    QueryBuilder node(String nodeName);
    QueryBuilder root();
    QueryBuilder begin();
    QueryBuilder end();
    Query build();
}