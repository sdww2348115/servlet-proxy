package com.sdww8591.servletproxy.delivery;

import com.sdww8591.servletproxy.ResponseCallback;
import com.sdww8591.servletproxy.entity.Request;
import com.sdww8591.servletproxy.entity.Response;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 传输接口
 * 用于将request发送出去，并获取response回应
 * 线程安全
 */
public interface HttpClient {

    /**
     * 用于将request封装为http请求并发送出去，并将得到的http回应封装为response
     * 可能同时被多个线程同步调用，请保证该方法的线程安全性
     * @param request 待发送的数据
     * @return 经过解析处理的http请求
     */
    Response execute(Request request, HttpServletResponse servletResponse) throws IOException;

    void registerResponseCallback(ResponseCallback responseCallback);
}
