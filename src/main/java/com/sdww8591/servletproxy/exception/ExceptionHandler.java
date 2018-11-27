package com.sdww8591.servletproxy.exception;

import com.sdww8591.servletproxy.entity.Response;

public interface ExceptionHandler {

    Response handleException(Exception e);
}
