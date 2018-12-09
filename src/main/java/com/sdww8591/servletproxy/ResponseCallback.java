package com.sdww8591.servletproxy;

import com.sdww8591.servletproxy.entity.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ResponseCallback {

    void callback(Response response, HttpServletResponse servletResponse) throws IOException;
}
