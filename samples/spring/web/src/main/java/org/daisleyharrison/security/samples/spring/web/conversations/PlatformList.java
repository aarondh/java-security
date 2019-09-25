package org.daisleyharrison.security.samples.spring.web.conversations;

import org.daisleyharrison.security.samples.spring.webtalker.TokenizedWebTalker;
import org.daisleyharrison.security.samples.spring.webtalker.WebTalkerParameter;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class PlatformList extends TokenizedWebTalker<Void, String> {
    
    @WebTalkerParameter("q")
    private String query;

    @WebTalkerParameter("p")
    private int pageNumber;

    @WebTalkerParameter("ps")
    private int pageSize;

    public PlatformList() {
        super(String.class, "platform-service", "api/v1/platform", HttpMethod.GET);
        setPageSize(42);
        setAuthenticationRequired(true);
        setPopRequired(true);
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
     * @return String return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

}