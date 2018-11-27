package com.sdww8591.servletproxy.entity;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class Response {

    private int statusCode;

    private Map<String, String> header;

    private InputStream body;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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
