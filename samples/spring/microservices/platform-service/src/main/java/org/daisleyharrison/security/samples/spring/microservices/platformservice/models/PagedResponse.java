package org.daisleyharrison.security.samples.spring.microservices.platformservice.models;

import java.util.List;

import java.util.ArrayList;

public class PagedResponse<T> {
    public static final int DEFAULT_PAGE_SIZE = 16;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    private int pageNumber;
    private int pageSize;
    private long totalSize;
    private List<T> data;

    public PagedResponse() {
        this.pageNumber = DEFAULT_PAGE_NUMBER;
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.totalSize = 0;
        this.data = new ArrayList<>();
    }

    public PagedResponse(int pageNumber, int pageSize, long totalSize, List<T> data) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
        this.data = new ArrayList<>(data);
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
     * @return int return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return long return the totalSize
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * @param totalSize the totalSize to set
     */
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * @return List<T> return the data
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<T> data) {
        this.data = data;
    }

}