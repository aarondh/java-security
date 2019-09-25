package org.daisleyharrison.security.samples.spring.webtalker;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Mono;

public class WebTalker<D, R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebTalker.class);

    private static Map<Class<?>, WebTalkerParamMap> s_maps = new HashMap<>();

    private String serviceEndPoint;
    private String microserviceName;
    private boolean secure;
    private HttpMethod method;
    private Class<R> resultType;
    private WebClient webClient;

    public WebTalker(Class<R> resultType) {
        this.resultType = resultType;
        if (!s_maps.containsKey(getClass())) {
            WebTalkerParamMap map = new WebTalkerParamMap(getClass());
            s_maps.put(getClass(), map);
        }
    }

    @Autowired
    MicroserviceUrlProvider microserviceLookup;

    public WebTalker(Class<R> resultType, String microserviceName, String serviceEndPoint, HttpMethod method) {
        this(resultType);
        this.microserviceName = microserviceName;
        this.serviceEndPoint = serviceEndPoint;
        this.method = method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod httpMethod) {
        this.method = httpMethod;
    }

    /**
     * @return String return the serviceEndPoint
     */
    public String getServiceEndpoint() {
        return serviceEndPoint;
    }

    /**
     * @param serviceEndPoint the serviceEndPointUri to set
     */
    public void setServiceEndpoint(String serviceEndPoint) {
        this.serviceEndPoint = serviceEndPoint;
    }

    /**
     * @return String return the microserviceName
     */
    public String getMicroserviceName() {
        return microserviceName;
    }

    /**
     * @param microserviceName the microserviceName to set
     */
    public void setMicoserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    protected void examineRequest(ClientRequest clientRequest) {

    }

    protected ExchangeFilterFunction filterRequest() {
        return (clientRequest, next) -> {
            examineRequest(clientRequest);
            return next.exchange(clientRequest);
        };
    }

    protected Optional<String> getMicroserviceUrl() {
        return microserviceLookup.lookupMicroservice(microserviceName, secure);
    }

    protected MultiValueMap<String, String> getQueryParams() {
        return s_maps.get(getClass()).getQueryParams(this);
    }

    protected MultiValueMap<String, String> getPathVariables() {
        return s_maps.get(getClass()).getPathVariables(this);
    }

    protected Map<String, String> getHeaders() {
        return s_maps.get(getClass()).getHeaders(this);
    }

    protected URI buildUri(UriBuilder uriBuilder) {
        return uriBuilder.path(getServiceEndpoint()).queryParams(getQueryParams()).build(getPathVariables());
    }

    protected void buildHeaders(HttpHeaders headers, Function<UriBuilder, URI> buildUri, D data) {
        getHeaders().forEach((name, value) -> {
            headers.add(name, value);
        });
    }

    protected WebClient getWebClient() {
        if (webClient == null) {
            Optional<String> microserviceUrl = getMicroserviceUrl();
            if(microserviceUrl.isPresent()){
                webClient = WebClient.builder().baseUrl(microserviceUrl.get()).filter(filterRequest()).build();
                LOGGER.info("Constructed WebClient for microservice \"{}\" with base URL: {}", getMicroserviceName(), microserviceUrl.get());
            }
            else {
                String message ="Microservice \"" + getMicroserviceName() + "\" was not available.";
                LOGGER.error(message);
                throw new IllegalStateException(message);
            }
        }
        return webClient;
    }

    protected Mono<R> talkWithHeaders(Function<UriBuilder, URI> produceUri, D data) {
        RequestBodySpec requestBodySpec = getWebClient().method(method).uri(produceUri)
                .headers((h) -> buildHeaders(h, produceUri, data));
        if (data == null) {
            return requestBodySpec.retrieve().bodyToMono(resultType);
        } else {
            BodyInserter<D, ReactiveHttpOutputMessage> inserter = BodyInserters.fromObject(data);
            return requestBodySpec.body(inserter).retrieve().bodyToMono(resultType);
        }
    }

    public class TalkSpec {
        private MultiValueMap<String, String> params;
        private MultiValueMap<String, String> variables;
        private WebTalker<D, R> talker;

        public TalkSpec(WebTalker<D, R> talker) {
            this.talker = talker;
            params = talker.getQueryParams();
            variables = talker.getPathVariables();
        }

        public TalkSpec param(String name, String value) {
            List<String> values = new ArrayList<>();
            values.add(value);
            if (params.containsKey(name)) {
                params.replace(name, values);
            } else if (variables.containsKey(name)) {
                variables.replace(name, values);
            } else {
                throw new IllegalArgumentException("Parameter \"" + name + "\" was not defined.");
            }
            return this;
        }

        public TalkSpec param(String name, int value) {
            return param(name, Integer.toString(value));
        }

        public TalkSpec param(String name, double value) {
            return param(name, Double.toString(value));
        }

        public TalkSpec param(String name, boolean value) {
            return param(name, value ? "true" : "false");
        }

        private URI setBuildUri(UriBuilder uriBuilder) {
            return uriBuilder.path(talker.getServiceEndpoint()).queryParams(params).build(variables);
        }

        public Mono<R> talk() {
            return talkWithHeaders(this::setBuildUri, null);
        }

        public Mono<R> talk(D data) {
            return talkWithHeaders(this::setBuildUri, null);
        }
    }

    public TalkSpec set() {
        return new TalkSpec(this);
    }

    public Mono<R> talk() {
        return talkWithHeaders(this::buildUri, null);
    }

    public Mono<R> talk(D data) {
        return talkWithHeaders(this::buildUri, data);
    }
}