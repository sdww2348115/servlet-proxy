package com.sdww8591.servletproxy;

import com.sdww8591.servletproxy.delivery.HttpClient;
import com.sdww8591.servletproxy.entity.Request;
import com.sdww8591.servletproxy.interceptor.RequestInterceptor;
import com.sdww8591.servletproxy.interceptor.ResponseInterceptor;
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

    /**
     * 非Spring环境请采用该方式进行构建
     * @param client
     * @param requestInterceptorChain
     * @param responseInterceptorChain
     */
    public static ServletProxyFilter build(HttpClient client, List<RequestInterceptor> requestInterceptorChain,
                                           List<ResponseInterceptor> responseInterceptorChain) {
        ServletProxyFilter filter = new ServletProxyFilter();
        filter.client = client;
        filter.requestInterceptorChain = requestInterceptorChain;
        filter.responseInterceptorChain = responseInterceptorChain;
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
            throw new RuntimeException("can not find httpClient");
        }
        log.info("httpClient assembling completed! class:{}", client.getClass());

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

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
    }

    @Override
    public void destroy() {

    }

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
}
