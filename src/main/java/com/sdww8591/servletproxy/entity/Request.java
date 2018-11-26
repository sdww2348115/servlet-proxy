package com.sdww8591.servletproxy.entity;

import org.springframework.http.HttpMethod;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Request {

    String url;

    String httpMethod;

    Map<String, String> header = new HashMap<>();

    InputStream body;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(InputStream body) {
        this.body = body;
    }
}
