package com.sdww8591.servletproxy;

import com.sdww8591.servletproxy.delivery.ApacheHttpClient;
import com.sdww8591.servletproxy.delivery.HttpClient;
import com.sdww8591.servletproxy.entity.Request;
import com.sdww8591.servletproxy.entity.Response;
import com.sdww8591.servletproxy.exception.DefaultExceptionHandler;
import com.sdww8591.servletproxy.exception.ExceptionHandler;
import com.sdww8591.servletproxy.interceptor.RequestInterceptor;
import com.sdww8591.servletproxy.interceptor.ResponseInterceptor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * ServletProxy工具引擎类
 */
public class ServletProxyFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ServletProxyFilter.class);

    private HttpClient client;

    private List<RequestInterceptor> requestInterceptorChain;

    private List<ResponseInterceptor> responseInterceptorChain;

    private ExceptionHandler exceptionHandler;

    private final ResponseCallback responseCallback = new ResponseCallback() {

        @Override
        public void callback(Response response, HttpServletResponse servletResponse) throws IOException {
            for (ResponseInterceptor interceptor: responseInterceptorChain) {
                if (interceptor.accept(response) && interceptor.process(response)) {
                    break;
                }
            }

            writeResponse(servletResponse, response);
        }
    };

    /**
     * 非Spring环境请采用该方式进行构建
     * @param client
     * @param requestInterceptorChain
     * @param responseInterceptorChain
     */
    public static ServletProxyFilter build(HttpClient client, List<RequestInterceptor> requestInterceptorChain,
                                           List<ResponseInterceptor> responseInterceptorChain, ExceptionHandler exceptionHandler) {
        ServletProxyFilter filter = new ServletProxyFilter();

        if (client == null) {
            filter.client = new ApacheHttpClient();
        } else {
            filter.client = client;
        }
        log.info("httpClient assembling completed! class:{}", filter.client.getClass());

        if (exceptionHandler == null) {
            filter.exceptionHandler = new DefaultExceptionHandler();
        } else {
            filter.exceptionHandler = exceptionHandler;
        }
        log.info("exceptionHandler assembling completed! class:{}", filter.exceptionHandler.getClass());

        if (requestInterceptorChain != null && requestInterceptorChain.size() != 0) {
            Collections.sort(requestInterceptorChain, Comparator.comparing(RequestInterceptor::getPriority));
            filter.requestInterceptorChain = Collections.unmodifiableList(requestInterceptorChain);
        } else {
            filter.requestInterceptorChain = Collections.emptyList();
        }
        log.info("request-interceptor chain assembling completed! RequestInterceptors:{}",
                filter.requestInterceptorChain.stream().map(interceptor -> interceptor.getClass()));

        if (responseInterceptorChain != null && responseInterceptorChain.size() != 0) {
            Collections.sort(responseInterceptorChain, Comparator.comparing(ResponseInterceptor::getPriority));
            filter.responseInterceptorChain = Collections.unmodifiableList(responseInterceptorChain);
        } else {
            filter.responseInterceptorChain = Collections.emptyList();
        }
        log.info("response-interceptor chain assembling completed! ResponseInterceptors:{}",
                responseInterceptorChain.stream().map(interceptor -> interceptor.getClass()));

        filter.client.registerResponseCallback(filter.responseCallback);
        return filter;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

       doFilter(req, resp, chain);
    }

    /**
     * servlet-proxy的核心处理逻辑，每个http请求到达filter后的生命周期如下：
     * 1.转化为Request
     * 2.依次通过RequestInterceptor
     * 3.执行外部请求
     * 4.依次执行ResponseInterceptor
     * 5.返回
     *
     * servlet-proxy采用同步阻塞方式执行核心逻辑，线程执行过程中的任何异常都需要用户自己去处理，任何一个interceptor失败都将导致最后返回错误
     * 可通过ExceptionHandler进行错误处理，并生成错误返回数据
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        try {

            Request req = toRequest(request);
            for (RequestInterceptor interceptor: requestInterceptorChain) {
                if (interceptor.accept(req) && interceptor.process(req)) {
                    break;
                }
            }

            client.execute(req, response);
        } catch (Exception e) {
            log.error("servlet proxy process error", e);
            writeResponse(response, exceptionHandler.handleException(e));
        }
    }

    /**
     * HttpServletRequst到Requst的转换类
     * @param httpServletRequest
     * @return
     * @throws IOException
     */
    private Request toRequest(HttpServletRequest httpServletRequest) throws IOException {

        if (httpServletRequest == null) {
            return null;
        }

        Request request = new Request();
        request.setUrl(httpServletRequest.getRequestURI());
        request.setHttpMethod(httpServletRequest.getMethod());

        List<String> headerNames = Util.enumerationToList(httpServletRequest.getHeaderNames());
        headerNames.stream().forEach(headerName -> {
            request.getHeader().put(headerName, httpServletRequest.getHeader(headerName));
        });

        request.setBody(httpServletRequest.getInputStream());
        return request;
    }

    /**
     * 将response中的数据进行返回
     * @param httpServletResponse
     * @param response
     * @throws IOException
     */
    private void writeResponse(HttpServletResponse httpServletResponse, Response response) throws IOException{
        httpServletResponse.setStatus(response.getStatusCode());
        for (Map.Entry<String, String> header: response.getHeader().entrySet()) {
            httpServletResponse.setHeader(header.getKey(), header.getValue());
        }
        if (Util.isInputStreamReadable(response.getBody())) {
            IOUtils.copyLarge(response.getBody(), httpServletResponse.getOutputStream());
        }
    }

}
