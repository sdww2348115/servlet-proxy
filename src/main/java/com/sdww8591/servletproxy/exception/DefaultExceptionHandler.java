package com.sdww8591.servletproxy.exception;

import com.sdww8591.servletproxy.entity.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.io.ByteArrayInputStream;

/**
 * 默认ExceptionHandler
 * 将构造一个status为500， body为：servlet proxy error的HTTP response进行返回
 */
@ConditionalOnMissingBean(ExceptionHandler.class)
public class DefaultExceptionHandler implements ExceptionHandler {

    private static final int DEFAULT_STATUS = 500;

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";

    private static final byte[] DEFAULT_RESP_BODY = "servlet proxy process error".getBytes();

    public Response handleException(Exception e) {
        Response exceptionResp = new Response();
        exceptionResp.setStatusCode(DEFAULT_STATUS);
        exceptionResp.getHeader().put(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        exceptionResp.setBody(new ByteArrayInputStream(DEFAULT_RESP_BODY));
        return exceptionResp;
    }
}
