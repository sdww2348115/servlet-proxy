package com.sdww8591.servletproxy;

import com.sdww8591.servletproxy.entity.Response;

public interface ExceptionHandler {

    Response handleException(Exception e);
}
