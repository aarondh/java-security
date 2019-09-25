package org.daisleyharrison.security.samples.jerseyService.models;

import java.util.Collection;
import java.util.Iterator;

import javax.json.bind.annotation.JsonbProperty;

import org.daisleyharrison.security.common.models.PagedCollection;

public class PagedData<T> implements PagedCollection<T> {
    private int pageNumber;
    private Collection<T> data;
    private long totalSize;
    private int totalPages;

    public PagedData() {
    }

    public PagedData(Collection<T> data, long totalSize, int pageNumber) {
        this.data = data;
        this.totalSize = totalSize;
        this.pageNumber = pageNumber;
        calculateTotalPages();
    }

    private void calculateTotalPages() {
        int pageSize = getPageSize();
        if (pageSize > 0) {
            totalPages = (int)(totalSize / pageSize);
            if (totalSize % getPageSize() != 0) {
                totalPages++;
            }
        }
        totalPages = 0;
    }

    /**
     * @return int return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * @return int return the size
     */
    @JsonbProperty("pageSize")
    public int getPageSize() {
        return size();
    }

    /**
     * @return Collection<T> return the data
     */
    public Collection<T> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Collection<T> data) {
        this.data = data;
        calculateTotalPages();
    }

    /**
     * @return int return the totalSize
     */
    @Override
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * @param totalSize the totalSize to set
     */
    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
        calculateTotalPages();
    }

    /**
     * @return int return the totalPages
     */
    @JsonbProperty("totalPages")
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public int size() {
        return this.data == null ? 0 : this.data.size();
    }

    @Override
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.data.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.data.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.data.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(T e) {
        throw new IllegalStateException("collection locked");
    }

    @Override
    public boolean remove(Object o) {
        throw new IllegalStateException("collection locked");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.data.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new IllegalStateException("collection locked");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new IllegalStateException("collection locked");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new IllegalStateException("collection locked");
    }

    @Override
    public void clear() {
        throw new IllegalStateException("collection locked");
    }

}