package org.daisleyharrison.security.samples.spring.webtalker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class WebTalkerParamMap {
    private Map<String, Method> queryMap;
    private Map<String, Method> pathVariableMap;
    private Map<String, Method> headerMap;

    private String fieldNametoGetMethodName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private String methodNameToFieldName(String methodName) {
        if (methodName.startsWith("get")) {
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        } else {
            return methodName;
        }
    }

    public WebTalkerParamMap(Class<?> type) {
        this.queryMap = new HashMap<>();
        this.pathVariableMap = new HashMap<>();
        this.headerMap = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {
            WebTalkerParameter talkParameter = field.getAnnotation(WebTalkerParameter.class);
            if (talkParameter != null) {
                String name;
                if (talkParameter.value().isEmpty()) {
                    name = field.getName();
                } else {
                    name = talkParameter.value();
                }
                try {
                    Method getMethod = type.getMethod(fieldNametoGetMethodName(field.getName()));
                    if (talkParameter.queryParam()) {
                        queryMap.put(name, getMethod);
                    } else if (talkParameter.pathVariable()) {
                        pathVariableMap.put(name, getMethod);
                    } else if (talkParameter.header()) {
                        headerMap.put(name, getMethod);
                    } else {
                        queryMap.put(name, getMethod);
                    }
                } catch (NoSuchMethodException ex) {

                }
            }
        }
        for (Method getMethod : type.getMethods()) {
            WebTalkerParameter talkParameter = getMethod.getAnnotation(WebTalkerParameter.class);
            if (talkParameter != null) {
                String name;
                if (talkParameter.value().isEmpty()) {
                    name = methodNameToFieldName(getMethod.getName());
                } else {
                    name = talkParameter.value();
                }
                if (talkParameter.queryParam()) {
                    queryMap.put(name, getMethod);
                } else if (talkParameter.pathVariable()) {
                    pathVariableMap.put(name, getMethod);
                } else if (talkParameter.header()) {
                    headerMap.put(name, getMethod);
                } else {
                    queryMap.put(name, getMethod);
                }

            }
        }
    }

    public MultiValueMap<String, String> getQueryParams(Object obj) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryMap.forEach((name, getMethod) -> {
            try {
                Object value = getMethod.invoke(obj);
                if (value != null) {
                    queryParams.add(name, value.toString());
                }
            } catch (IllegalAccessException | InvocationTargetException ex) {

            }
        });
        return queryParams;
    }

    public MultiValueMap<String, String> getPathVariables(Object obj) {
        MultiValueMap<String, String> pathVariables = new LinkedMultiValueMap<>();
        pathVariableMap.forEach((name, getMethod) -> {
            try {
                Object value = getMethod.invoke(obj);
                if (value != null) {
                    pathVariables.add(name, value.toString());
                }
            } catch (IllegalAccessException | InvocationTargetException ex) {

            }
        });
        return pathVariables;
    }

    public Map<String, String> getHeaders(Object obj) {
        Map<String, String> headers = new HashMap<>();
        headerMap.forEach((name, getMethod) -> {
            try {
                Object value = getMethod.invoke(obj);
                if (value != null) {
                    headers.put(name, value.toString());
                }
            } catch (IllegalAccessException | InvocationTargetException ex) {

            }
        });
        return headers;
    }
}
