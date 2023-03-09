package web;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.util.List;

public class Request {
    private String method;
    private String path;
    private List<String> headers;
    private byte[] body;

    private URIBuilder uriBuilder;

    public Request(String method, String path, List<String> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public Request(String method, URIBuilder uriBuilder) {
        this.method = method;
        this.uriBuilder = uriBuilder;
        path = uriBuilder.getPath();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
        return uriBuilder.getQueryParams();
    }

    public NameValuePair getQueryParam(String name) {
        return uriBuilder.getFirstQueryParam(name);
    }
}
