package org.daisleyharrison.security.services.datastore.mongodb.models;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

import org.daisleyharrison.security.common.models.datastore.Query;

import org.daisleyharrison.security.common.models.datastore.QueryBuilder;

@SuppressWarnings("rawtypes")
public class QueryBuilderImpl implements QueryBuilder {
    private static final SimpleDateFormat ISO_DATE_TIME_FORMATTER;
    static {
        ISO_DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ISO_DATE_TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private Class type;
    private Field propertyField;
    private StringBuilder query = new StringBuilder();

    private enum Mode {
        NEW, NODE, PROPERTY, OPERATOR, EXPRESSION, BEGIN, OR, AND
    }

    private interface EndAction {
        public QueryBuilder end(String expression);
    }

    private Mode mode;
    private EndAction endAction;
    private List<String> orQueries;
    private List<String> andQueries;
    private boolean autoEnd;

    public QueryBuilderImpl(Class type) {
        this.type = type;
        this.propertyField = null;
        mode = Mode.NEW;
    }

    protected QueryBuilderImpl(Class type, EndAction endAction) {
        this.type = type;
        this.propertyField = null;
        this.endAction = endAction;
        mode = Mode.NODE;
    }

    private void setAutoEnd(boolean autoEnd){
        this.autoEnd = autoEnd;
    }

    private void throwUnexpectedState() throws IllegalStateException {
        throw new IllegalStateException("Unexpected state " + mode.toString());
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
        case AND:
            throw new IllegalStateException("Expecting an and() or or() function here");
        case OR:
            throw new IllegalStateException("Expecting an or() or and() function here");
        default:
            throwUnexpectedState();
        }
    }

    private Field validateProperty(String propertyName) {
        if (propertyName == null || propertyName.isBlank()) {
            throw new IllegalArgumentException("propertyName cannot be null or blank");
        }
        Class<?> i = type;
        while (i != null && i != Object.class) {
            try {
                return i.getDeclaredField(propertyName);
            } catch (NoSuchFieldException ex) {
                i = i.getSuperclass();
            }
        }
        throw new IllegalArgumentException(
                "property \"" + propertyName + "\" not found on type " + type.getSimpleName());
    }

    @Override
    public QueryBuilder property(String propertyName) {
        assertMode(Mode.NODE, Mode.BEGIN, Mode.EXPRESSION);
        propertyField = validateProperty(propertyName);
        switch (mode) {
        case NODE:
        case BEGIN:
            query.append("{");
            mode = Mode.PROPERTY;
            break;
        case EXPRESSION:
            query.append(",");
            mode = Mode.PROPERTY;
            break;
        default:
            throwUnexpectedState();
        }
        query.append(wrapPropertyName(propertyName));
        query.append(": ");
        mode = Mode.OPERATOR;
        return this;
    }

    private static String escapeString(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
            case '\b':
                result.append("\\b");
                break;
            case '\f':
                result.append("\\f");
                break;
            case '\n':
                result.append("\\n");
                break;
            case '\r':
                result.append("\\r");
                break;
            case '\t':
                result.append("\\t");
                break;
            case '\"':
                result.append("\\\"");
                break;
            case '\\':
                result.append("\\\\");
                break;
            default:
                result.append(c);
            }
        }
        return result.toString();
    }

    private void applyValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (!propertyField.getType().isInstance(value)) {
            throw new IllegalArgumentException("values must be of type " + propertyField.getType().getSimpleName());
        }
        if (value instanceof String) {
            query.append("\"");
            query.append(escapeString((String) value));
            query.append("\"");
        } else if (value instanceof Instant) {
            query.append(ISO_DATE_TIME_FORMATTER.format((Instant) value));
        } else if (value instanceof Date) {
            query.append("{$date: \"");
            query.append(ISO_DATE_TIME_FORMATTER.format((Date) value));
            query.append("\"}");
        } else if (value instanceof ZonedDateTime) {
            query.append("new Date(\"");
            query.append(DateTimeFormatter.ISO_DATE_TIME.format((ZonedDateTime) value));
            query.append("\")");
        } else if (value instanceof UUID) {
            query.append("UUID(\"");
            query.append(((UUID) value).toString());
            query.append("\")");
        } else if (value instanceof Number) {
            query.append(value);
        } else if (value instanceof Boolean) {
            if ((boolean) value) {
                query.append("true");
            } else {
                query.append("false");
            }
        }
    }

    private String wrapPropertyName(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName cannot be null");
        }
        String escaped = escapeString(propertyName);
        if (escaped.equals(propertyName) && !propertyName.contains(".")) {
            return propertyName;
        }
        return "\"" + escaped + "\"";

    }
    private QueryBuilder endOperator(){
        mode = Mode.EXPRESSION;
        if(autoEnd){
            return end();
        }
        return this;
    }
    private QueryBuilder operator(String operator, Object value) {
        assertMode(Mode.OPERATOR);
        if (!propertyField.getType().isInstance(value)) {
            throw new IllegalArgumentException(
                    "value was expected to be of type " + propertyField.getType().getSimpleName());
        }
        if (operator == "=") {
            applyValue(value);
        } else {
            query.append("{\"");
            query.append(operator);
            query.append("\":");
            applyValue(value);
            query.append("}");
        }
        return endOperator();
    }

    @Override
    public QueryBuilder is(Object value) {
        return operator("=", value);
    }

    @Override
    public QueryBuilder isNot(Object value) {
        return operator("$ne", value);
    }

    @Override
    public QueryBuilder lessThan(Object value) {
        return operator("$lt", value);
    }

    @Override
    public QueryBuilder lessOrEqual(Object value) {
        return operator("$le", value);
    }

    @Override
    public QueryBuilder greaterThan(Object value) {
        return operator("$gt", value);
    }

    @Override
    public QueryBuilder greaterOrEqual(Object value) {
        return operator("$ge", value);
    }

    @Override
    public QueryBuilder contains(String value) {
        assertMode(Mode.OPERATOR);
        if (propertyField.getType() != String.class) {
            throw new IllegalStateException("This operator can only be apply to String fields");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (value instanceof String) {
            query.append("/");
            query.append(escapeString((String) value));
            query.append("/");
        }
        return endOperator();
    }

    @Override
    public QueryBuilder matches(String value) {
        assertMode(Mode.OPERATOR);
        if (propertyField.getType() != String.class) {
            throw new IllegalStateException("This operator can only be apply to String fields");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (value instanceof String) {
            query.append("/^");
            query.append(escapeString((String) value));
            query.append("$/");
        }
        return endOperator();
    }

    @Override
    public QueryBuilder startsWith(String value) {
        assertMode(Mode.OPERATOR);
        if (propertyField.getType() != String.class) {
            throw new IllegalStateException("This operator can only be apply to String fields");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        query.append("/^");
        query.append(escapeString((String) value));
        query.append("/");
        return endOperator();
    }

    @Override
    public QueryBuilder endsWith(String value) {
        assertMode(Mode.OPERATOR);
        if (propertyField.getType() != String.class) {
            throw new IllegalStateException("This operator can only be apply to String fields");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        query.append("/");
        query.append(escapeString((String) value));
        query.append("$/");
        return endOperator();
    }

    @Override
    public QueryBuilder isNull() {
        assertMode(Mode.OPERATOR);
        query.append("null");
        assertMode(Mode.EXPRESSION);
        return endOperator();
    }

    @Override
    public QueryBuilder isNotNull() {
        assertMode(Mode.OPERATOR);
        query.append("$ne null");
        assertMode(Mode.EXPRESSION);
        return endOperator();
    }

    @Override
    public QueryBuilder in(Object... values) {
        assertMode(Mode.OPERATOR);
        if (values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        query.append("$in: [");
        boolean first = true;
        for (Object value : values) {
            if (first) {
                first = false;
            } else {
                query.append(",");
            }
            applyValue(value);
        }
        query.append("]");
        return endOperator();
    }

    @Override
    public QueryBuilder notIn(Object... values) {
        assertMode(Mode.OPERATOR);
        if (values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        query.append("$nin: [");
        boolean first = true;
        for (Object value : values) {
            if (first) {
                first = false;
            } else {
                query.append(",");
            }
            applyValue(value);
        }
        query.append("]");
        return endOperator();
    }

    private String produceAndQuery() {
        assertMode(Mode.AND);
        String result;
        if (andQueries == null || andQueries.isEmpty()) {
            result = "";
        } else if (andQueries.size() == 1) {
            result = andQueries.get(0).toString();
        } else {
            StringBuilder andQuery = new StringBuilder();
            andQuery.append("{$and: [");
            andQuery.append(
                    String.join(",", andQueries.stream().map(b -> b.toString()).collect(Collectors.joining(","))));
            andQuery.append("]}");
            result = andQuery.toString();
        }
        andQueries = null;
        mode = Mode.NODE;
        return result;

    }

    private String produceOrQuery() {
        assertMode(Mode.OR);
        String result;
        if (orQueries == null || orQueries.isEmpty()) {
            result = "";
        } else if (orQueries.size() == 1) {
            result = orQueries.get(0).toString();
        } else {
            StringBuilder orQuery = new StringBuilder();
            orQuery.append("{$or: [");
            orQuery.append(String.join(",", orQueries.stream().map(b -> b.toString()).collect(Collectors.joining(","))));
            orQuery.append("]}");
            result = orQuery.toString();
        }
        orQueries = null;
        mode = Mode.NODE;
        return result;
    }

    @Override
    public QueryBuilder and() {
        assertMode(Mode.OR, Mode.AND, Mode.EXPRESSION);
        switch (mode) {
        case EXPRESSION:
            query.append("}");
            mode = Mode.NODE;
            andQueries = new ArrayList<>();
            andQueries.add(query.toString());
            query.setLength(0);
            mode = Mode.AND;
            break;
        case AND:
            query.setLength(0);
            break;
        case OR:
            andQueries = new ArrayList<>();
            andQueries.add(produceOrQuery());
            query.setLength(0);
            mode = Mode.AND;
            break;
        default:
        }
        final QueryBuilder me = this;
        QueryBuilderImpl andBuilder = new QueryBuilderImpl(this.type, expression -> {
            andQueries.add(expression);
            return me;
        });
        andBuilder.mode = Mode.BEGIN;
        return andBuilder;
    }

    @Override
    public QueryBuilder and(String propertyName) {
        if(mode == Mode.EXPRESSION){
            return property(propertyName);
        }
        QueryBuilder andBuilder = and();
        andBuilder.property(propertyName);
        ((QueryBuilderImpl)andBuilder).setAutoEnd(true);
        return andBuilder;
    }


    @Override
    public QueryBuilder or() {
        assertMode(Mode.OR, Mode.AND, Mode.EXPRESSION);
        switch (mode) {
        case EXPRESSION:
            query.append("}");
            mode = Mode.NODE;
            orQueries = new ArrayList<>();
            orQueries.add(query.toString());
            query.setLength(0);
            mode = Mode.OR;
            break;
        case OR:
            query.setLength(0);
            break;
        case AND:
            orQueries = new ArrayList<>();
            orQueries.add(produceAndQuery());
            query.setLength(0);
            mode = Mode.OR;
            break;
        default:
        }
        final QueryBuilder me = this;
        QueryBuilderImpl orBuilder = new QueryBuilderImpl(this.type, expression -> {
            orQueries.add(expression);
            return me;
        });
        orBuilder.mode = Mode.BEGIN;
        return orBuilder;
    }

    @Override
    public QueryBuilder or(String propertyName) {
        QueryBuilder orBuilder = or();
        orBuilder.property(propertyName);
        ((QueryBuilderImpl)orBuilder).setAutoEnd(true);
        return orBuilder;
    }

    @Override
    public QueryBuilder node(String nodeName) {
        assertMode(Mode.NEW, Mode.NODE, Mode.EXPRESSION);
        if (nodeName == null) {
            throw new IllegalArgumentException("nodeName cannot be null");
        }
        if (mode == Mode.EXPRESSION) {
            query.append(",");
        } else {
            query.append("{");
        }
        propertyField = validateProperty(nodeName);
        query.append(wrapPropertyName(nodeName));
        query.append(":");
        mode = Mode.EXPRESSION;
        final QueryBuilder me = this;
        return new QueryBuilderImpl(propertyField.getType(), expression -> {
            query.append(expression);
            return me;
        });
    }

    @Override
    public QueryBuilder root() {
        assertMode(Mode.NEW);
        mode = Mode.NODE;
        return this;
    }

    @Override
    public QueryBuilder begin() {
        assertMode(Mode.EXPRESSION);
        final QueryBuilder me = this;
        return new QueryBuilderImpl(this.type, (expression) -> {
            this.query.append(expression);
            return me;
        });
    }

    @Override
    public QueryBuilder end() {
        assertMode(Mode.EXPRESSION, Mode.AND, Mode.OR);
        switch (mode) {
        case EXPRESSION:
            query.append("}");
            break;
        case AND:
            query.append(produceAndQuery());
            break;
        case OR:
            query.append(produceOrQuery());
            break;
        default:
        }
        mode = Mode.NODE;
        return this.endAction.end(this.query.toString());
    }

    private class QueryImpl implements Query {
        private String query;

        public QueryImpl(String query) {
            this.query = query;
        }

        @Override
        public String toString() {
            // return "{ query: " + query + "}";
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
            // if (depth < 0) {
            // throw new IllegalStateException("No nodes defined");
            // }
            break;
        case AND:
            query.append(produceAndQuery());
            break;
        case OR:
            query.append(produceOrQuery());
            break;
        case BEGIN:
        case PROPERTY:
        case OPERATOR:
            throw new IllegalStateException("Incomplete property expression");
        case EXPRESSION:
            query.append("}");
            break;
        default:
            throwUnexpectedState();
        }
        return new QueryImpl(this.query.toString());
    }
}