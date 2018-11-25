package com.sdww8591.servletproxy.entity;

import org.springframework.http.HttpMethod;

import java.io.InputStream;
import java.util.Map;

public class Request {

    String uri;

    HttpMethod httpMethod;

    Map<String, String> header;

    InputStream body;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
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
