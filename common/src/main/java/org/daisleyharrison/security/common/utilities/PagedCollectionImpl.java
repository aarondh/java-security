package org.daisleyharrison.security.common.utilities;

import java.util.LinkedList;

import org.daisleyharrison.security.common.models.PagedCollection;

public class PagedCollectionImpl<T> extends LinkedList<T> implements PagedCollection<T> {
    private static final long serialVersionUID = -7420014733665418982L;
    private long totalSize;

    public PagedCollectionImpl(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
