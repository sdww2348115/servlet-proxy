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
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * ServletProxy工具引擎类
 */
public class ServletProxyFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(ServletProxyFilter.class);

    private HttpClient client;

    private List<RequestInterceptor> requestInterceptorChain;

    private List<ResponseInterceptor> responseInterceptorChain;

    private ExceptionHandler exceptionHandler;

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
        }
        filter.client = client;
        log.info("httpClient assembling completed! class:{}", client.getClass());

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

        return filter;
    }

    /**
     * Spring环境将默认采用该方法进行装配
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new RuntimeException("can not find applicationContext");
        }

        client = context.getBean(HttpClient.class);
        if (client == null) {
            client = new ApacheHttpClient();
        }
        log.info("httpClient assembling completed! class:{}", client.getClass());

        exceptionHandler = context.getBean(ExceptionHandler.class);
        if (exceptionHandler == null) {
            exceptionHandler = new DefaultExceptionHandler();
        }
        log.info("exceptionHandler assembling completed! class:{}", exceptionHandler.getClass());

        Collection<RequestInterceptor> requestInterceptorCollection = context.getBeansOfType(RequestInterceptor.class).values();
        List<RequestInterceptor> requestInterceptors = new ArrayList<>(requestInterceptorCollection);
        Collections.sort(requestInterceptors, Comparator.comparing(RequestInterceptor::getPriority));
        requestInterceptorChain = Collections.unmodifiableList(requestInterceptors);
        log.info("request-interceptor chain assembling completed! RequestInterceptors:{}",
                requestInterceptorChain.stream().map(interceptor -> interceptor.getClass()));

        log.info("assembling requestInterceptor chain...");
        Collection<ResponseInterceptor> responseInterceptorCollection = context.getBeansOfType(ResponseInterceptor.class).values();
        List<ResponseInterceptor> responseInterceptors = new ArrayList<>(responseInterceptorCollection);
        Collections.sort(responseInterceptors, Comparator.comparing(ResponseInterceptor::getPriority));
        responseInterceptorChain = Collections.unmodifiableList(responseInterceptors);
        log.info("response-interceptor chain assembling completed! ResponseInterceptors:{}",
                responseInterceptorChain.stream().map(interceptor -> interceptor.getClass()));
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
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            Request req = toRequest(request);
            for (RequestInterceptor interceptor: requestInterceptorChain) {
                if (interceptor.accept(req) && interceptor.process(req)) {
                    break;
                }
            }

            Response resp = client.send(req);
            for (ResponseInterceptor interceptor: responseInterceptorChain) {
                if (interceptor.accept(resp) && interceptor.process(resp)) {
                    break;
                }
            }

            writeResponse(response, resp);
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
        IOUtils.copyLarge(response.getBody(), httpServletResponse.getOutputStream());
    }

}
