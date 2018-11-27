package com.sdww8591.servletproxy.delivery;

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

import java.io.IOException;
import java.net.URI;

public class ApacheHttpClient implements HttpClient {

    private final CloseableHttpClient client = HttpClients.createDefault();

    /**
     * 用于将HTTP response处理为Response的handler类
     */
    private final ResponseHandler<Response> responseHandler = new ResponseHandler<Response>() {

        @Override
        public Response handleResponse(HttpResponse httpResponse) throws IOException {
            Response response = new Response();
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            for (Header header: httpResponse.getAllHeaders()) {
                response.getHeader().put(header.getName(), header.getValue());
            }
            response.setBody(httpResponse.getEntity().getContent());
            return response;
        }

    };

    @Override
    public Response send(Request request) throws IOException {
        GenericHttpMethod genericHttpMethod = new GenericHttpMethod();
        genericHttpMethod.setMethod(request.getHttpMethod());
        genericHttpMethod.setURI(URI.create(request.getUrl()));
        request.getHeader().entrySet().stream().filter(entry -> {
            return !"Content-Length".equalsIgnoreCase(entry.getKey());
        }).forEach(entry -> {
            genericHttpMethod.addHeader(entry.getKey(), entry.getValue());
        });
        genericHttpMethod.setEntity(new InputStreamEntity(request.getBody()));

        return client.execute(genericHttpMethod, responseHandler);
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
