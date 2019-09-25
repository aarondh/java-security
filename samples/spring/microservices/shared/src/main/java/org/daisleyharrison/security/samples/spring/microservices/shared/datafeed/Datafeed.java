package org.daisleyharrison.security.samples.spring.microservices.shared.datafeed;

import java.io.IOException;
import java.io.InputStream;

public interface Datafeed<T> {
    public interface Action<E> {
        public boolean action(E entity);
    }
    public DatafeedMetaData getMetaData();
    public void parse(InputStream inputStream, Action<T> action) throws IOException;
}