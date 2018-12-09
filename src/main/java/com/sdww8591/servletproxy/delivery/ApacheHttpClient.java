package com.sdww8591.servletproxy.delivery;

import com.sdww8591.servletproxy.ResponseCallback;
import com.sdww8591.servletproxy.Util;
import com.sdww8591.servletproxy.entity.Request;
import com.sdww8591.servletproxy.entity.Response;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class ApacheHttpClient implements HttpClient {

    private final CloseableHttpClient client = HttpClients.createDefault();

    private ResponseCallback responseCallback;

    @Override
    public Response execute(Request request, HttpServletResponse servletResponse) throws IOException {
        GenericHttpMethod genericHttpMethod = new GenericHttpMethod();
        genericHttpMethod.setMethod(request.getHttpMethod());
        genericHttpMethod.setURI(URI.create(request.getUrl()));
        request.getHeader().entrySet().stream().filter(entry -> {
            return Util.predicateHeader(entry);
        }).forEach(entry -> {
            genericHttpMethod.addHeader(entry.getKey(), entry.getValue());
        });
        genericHttpMethod.setEntity(new InputStreamEntity(request.getBody()));

        return client.execute(genericHttpMethod, httpResponse -> {
            Response response = new Response();
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            for (Header header: httpResponse.getAllHeaders()) {
                response.getHeader().put(header.getName(), header.getValue());
            }
            response.setBody(httpResponse.getEntity().getContent());

            //所有请求需要在这里执行完毕
            if (responseCallback == null) {
                throw new RuntimeException("responseCallback has not initialized!");
            }
            responseCallback.callback(response, servletResponse);
            return null;
        });
    }

    @Override
    public void registerResponseCallback(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    /**
     * ApacheHttpClient中缺乏通用method请求，这里构建一个wrapper类进行简单处理
     */
    private static class GenericHttpMethod extends HttpEntityEnclosingRequestBase {

        private String method;

        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public String getMethod() {
            return this.method;
        }
    }
}
