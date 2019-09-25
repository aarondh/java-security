package org.daisleyharrison.security.services.datastore.jsondb.models;

import java.lang.reflect.Field;
import java.util.Date;

import org.daisleyharrison.security.common.models.datastore.Query;

import org.daisleyharrison.security.common.models.datastore.QueryBuilder;

public class QueryBuilderImpl<T> implements QueryBuilder {
    private Class<T> type;
    private Field idField;
    private StringBuilder query = new StringBuilder();

    private enum Mode {
        NEW, NODE, PROPERTY, OPERATOR, EXPRESSION, BEGIN
    }

    private interface EndAction {
        public QueryBuilder end(String expression);
    }

    private Mode mode;
    private int depth;
    private EndAction endAction;

    public QueryBuilderImpl(Class<T> type, Field idField) {
        this.type = type;
        this.idField = idField;
        mode = Mode.NODE;
        depth = -1;
    }

    protected QueryBuilderImpl(Class<T> type, EndAction endAction) {
        this.type = type;
        this.endAction = endAction;
        depth = 0;
        mode = Mode.PROPERTY;
    }

    private void throwUnexpectedState() throws IllegalStateException {
        throw new IllegalStateException("Unexpected state" + mode.toString());
    }

    private void assertMode(Mode... assertedModes) throws IllegalStateException {
        for (Mode assertedMode : assertedModes) {
            if (assertedMode == this.mode) {
                return;
            }
        }
        switch (this.mode) {
        case NEW:
            throw new IllegalStateException("Expecting an root() or node() here");
        case NODE:
            throw new IllegalStateException("Expecting an or node() or property() here");
        case PROPERTY:
            throw new IllegalStateException("Expecting an property() here");
        case OPERATOR:
            throw new IllegalStateException("Expecting an start(), and(), or() here");
        case EXPRESSION:
            throw new IllegalStateException("Expecting an expression starting with property(), and(), or() here");
        case BEGIN:
            throw new IllegalStateException("Expecting an expression starting with property() here");
        default:
            throwUnexpectedState();
        }
    }

    @Override
    public QueryBuilder property(String propertyName) {
        assertMode(Mode.NEW, Mode.NODE, Mode.PROPERTY);
        if (propertyName == null || propertyName.isBlank()) {
            throw new IllegalArgumentException("propertyName cannot be null or blank");
        }
        switch (mode) {
        case NEW:
        case NODE:
            query.append("[");
            mode = Mode.PROPERTY;
        case PROPERTY:
            query.append(propertyName);
            mode = Mode.OPERATOR;
            break;
        default:
            throwUnexpectedState();
        }
        return this;
    }

    private void operator(String operator, Object value) {
        switch (mode) {
        case NEW:
        case NODE:
            query.append("[.");
            mode = Mode.OPERATOR;
        default:
        }
        assertMode(Mode.OPERATOR);
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        query.append(operator);
        if (value instanceof String) {
            query.append("'");
            query.append(value);
            query.append("'");
        } else if (value instanceof Date) {
            query.append(((Date) value).getTime());
        } else if (value instanceof Number) {
            query.append(value);
        }
        mode = Mode.EXPRESSION;
    }

    @Override
    public QueryBuilder is(Object value) {
        operator("=", value);
        return this;
    }

    @Override
    public QueryBuilder isNot(Object value) {
        operator("!=", value);
        return this;
    }

    @Override
    public QueryBuilder lessThan(Object value) {
        operator("<", value);
        return this;
    }

    @Override
    public QueryBuilder lessOrEqual(Object value) {
        operator("<=", value);
        return this;
    }

    @Override
    public QueryBuilder greaterThan(Object value) {
        operator(">", value);
        return this;
    }

    @Override
    public QueryBuilder greaterOrEqual(Object value) {
        operator(">=", value);
        return this;
    }

    @Override
    public QueryBuilder and(String propertyName) {
        assertMode(Mode.EXPRESSION);
        query.append(" and ");
        query.append(propertyName);
        mode = Mode.OPERATOR;
        return this;
    }

    @Override
    public QueryBuilder or(String propertyName) {
        assertMode(Mode.EXPRESSION);
        query.append(" or ");
        query.append(propertyName);
        mode = Mode.OPERATOR;
        return this;
    }

    @Override
    public QueryBuilder and() {
        assertMode(Mode.EXPRESSION);
        query.append(" and ");
        mode = Mode.BEGIN;
        return this;
    }

    @Override
    public QueryBuilder or() {
        assertMode(Mode.EXPRESSION);
        query.append(" or ");
        mode = Mode.BEGIN;
        return this;
    }

    @Override
    public QueryBuilder node(String nodeName) {
        assertMode(Mode.NEW, Mode.NODE, Mode.EXPRESSION);
        if (mode == Mode.EXPRESSION) {
            query.append("]");
        }
        query.append("/");
        if (nodeName == null) {
            query.append(".");
        } else {
            query.append(nodeName);
        }
        mode = Mode.NODE;
        depth = depth + 1;
        return this;
    }

    @Override
    public QueryBuilder root() {
        return node(null);
    }

    @Override
    public QueryBuilder begin() {
        assertMode(Mode.EXPRESSION);
        return new QueryBuilderImpl<T>(this.type, (expression) -> {
            this.query.append("(");
            this.query.append(expression);
            this.query.append(")");
            return this;
        });
    }

    @Override
    public QueryBuilder end() {
        assertMode(Mode.EXPRESSION);
        return this.endAction.end(this.query.toString());
    }

    private class QueryImpl implements Query {
        private String query;

        public QueryImpl(String query) {
            this.query = query;
        }

        @Override
        public String toString() {
            return query;
        }
    }

    @Override
    public Query build() {
        if (this.endAction != null) {
            throw new IllegalStateException("Missing end()");
        }
        switch (mode) {
        case NODE:
            if (depth < 0) {
                throw new IllegalStateException("No nodes defined");
            }
            break;
        case BEGIN:
        case PROPERTY:
        case OPERATOR:
            throw new IllegalStateException("Incomplete property expression");
        case EXPRESSION:
            query.append("]");
            break;
        default:
            throwUnexpectedState();
        }
        while (depth-- > 0) {
            query.append("/..");
        }
        return new QueryImpl(this.query.toString());
    }

    @Override
    public QueryBuilder contains(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBuilder matches(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBuilder startsWith(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBuilder endsWith(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBuilder in(Object... values) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBuilder notIn(Object... values) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBuilder isNull() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBuilder isNotNull() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}