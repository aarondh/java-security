package org.daisleyharrison.security.common.models;

import java.util.Collection;

public interface PagedCollection<T> extends Collection<T> {
    public long getTotalSize();
}