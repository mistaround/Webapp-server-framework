package edu.upenn.cis.cis455.m2.server;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class HttpResponse extends Response {
    static final Logger logger = LogManager.getLogger(HttpResponse.class);
    Map<String, String> headers = new HashMap<>();
    ArrayList<String> cookies = new ArrayList<>();
    public HttpResponse() {}

    @Override
    public void header(String header, String value) {
        headers.put(header, value);
    }

    @Override
    public void redirect(String location) {
        header("Location", location);
        this.status(302);
    }

    @Override
    public void redirect(String location, int httpStatusCode) {
        header("Location", location);
        this.status(httpStatusCode);
    }

    @Override
    public void cookie(String name, String value) {
        cookies.add(name + "=" + value);
    }

    @Override
    public void cookie(String name, String value, int maxAge) {
        cookies.add(name+"=" + value + "; Max-Age=" + maxAge);
    }

    @Override
    public void cookie(String name, String value, int maxAge, boolean secured) {
        cookies.add(name+"=" + value + "; Max-Age=" + maxAge + (secured ? "; Secure" : ""));
    }

    @Override
    public void cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        cookies.add(name+"=" + value + "; Max-Age=" + maxAge + (secured ? "; Secure" : "") + (httpOnly ? "; HttpOnly" : ""));
    }

    @Override
    public void cookie(String path, String name, String value) {
        cookies.add(name + "=" + value + "; Path=" + path);
    }

    @Override
    public void cookie(String path, String name, String value, int maxAge) {
        cookies.add(name + "=" + value + "; Path=" + path + "; Max-Age=" + maxAge);
    }

    @Override
    public void cookie(String path, String name, String value, int maxAge, boolean secured) {
        cookies.add(name + "=" + value + "; Path=" + path + "; Max-Age=" + maxAge + (secured ? "; Secure" : ""));
    }

    @Override
    public void cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        cookies.add(name + "=" + value + "; Path=" + path + "; Max-Age=" + maxAge + (secured ? "; Secure" : "") + (httpOnly ? "; HttpOnly" : ""));
    }

    @Override
    public void removeCookie(String name) {
        cookies.add(name + "= " + "; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    @Override
    public void removeCookie(String path, String name) {
        cookies.add(name + "= " + "; Path=" + path + "; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    @Override
    public Map<String, String> getHeaders() {
        int length = cookies.size();
        for (int i = 0; i < length; i++) {
            headers.put("Set-Cookie" + i, cookies.get(i));
        }
        return headers;
    }
}
