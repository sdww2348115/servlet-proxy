package com.sdww8591.servletproxy.entity;

import java.io.OutputStream;
import java.util.Map;

public class Response {

    private int statusCode;

    private Map<String, String> header;

    private OutputStream body;

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

    public OutputStream getBody() {
        return body;
    }

    public void setBody(OutputStream body) {
        this.body = body;
    }
}
