package com.sdww8591.servletproxy.exception;

import com.sdww8591.servletproxy.entity.Response;

/**
 * 用于对servlet-proxy执行过程中抛出的exception进行处理
 * 返回的Response将被封装为HTTP response返回给最初的调用方
 */
public interface ExceptionHandler {

    Response handleException(Exception e);
}
