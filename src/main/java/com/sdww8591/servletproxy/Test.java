package com.sdww8591.servletproxy;

import com.sdww8591.servletproxy.entity.Request;
import com.sdww8591.servletproxy.interceptor.RequestInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class Test {

    @Bean
    public FilterRegistrationBean registryServletProxyFilter() {
        ServletProxyFilter filter = ServletProxyFilter.build(null, Collections.singletonList(new RequestInterceptor() {

            private final static String HOST = "http://localhost:8081/upload";

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public boolean accept(Request request) {
                return true;
            }

            @Override
            public boolean process(Request request) {
                request.setUrl(HOST);
                return false;
            }
        }), Collections.emptyList(), null);
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.addUrlPatterns("/upload/*");
        registration.setName("servletProxyFilter");
        registration.setOrder(1);
        return registration;
    }
}
