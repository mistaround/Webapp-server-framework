package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.SparkController;
import edu.upenn.cis.cis455.m2.Route.RouteEntry;
import edu.upenn.cis.cis455.m2.Route.RouteImpl;
import edu.upenn.cis.cis455.m2.Session.SessionImpl;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Session;

import java.net.Socket;
import java.util.*;

public class HttpRequest extends Request {
    public Socket socket;
    public Map<String, String> pre;
    public Map<String, List<String>> parms;
    public Map<String, String> headers;
    public Map<String, Object> attrs;
    public String body = null;
    public RouteImpl route;
    public SessionImpl session;

    public HttpRequest(Socket socket, Map<String, String> pre, Map<String, List<String>> parms,
                       Map<String, String> headers, Map<String, String> body) {
        this.socket = socket;
        this.pre = pre;
        this.parms = parms;
        this.headers = headers;
        this.attrs = new HashMap<>();
        if (body.containsKey("body")) {
            this.body = body.get("body");
        }
    }

    public void workOnRoute(RouteImpl route) {
        this.route = route;
    }

    @Override
    public Session session() {
        if (headers.containsKey("cookie")) {
            String cookie = headers.get("cookie");
            if (cookie.contains("JSESSIONID=")) {
                int p = cookie.indexOf("JSESSIONID=");
                String tmp = cookie.substring(p + "JSESSIONID=".length());
                p = tmp.indexOf(";");
                String SID;
                if (p != -1) {
                    SID = tmp.substring(0, p - 1).trim();
                } else {
                    SID = tmp.trim();
                }
                if (!SparkController.webService.isValidSession(SID)) {
                    SID = SparkController.webService.createSession();
                    session = (SessionImpl) SparkController.webService.getSession(SID);
                    return session;
                }
                session = (SessionImpl) SparkController.webService.getSession(SID);
                return session;
            }
        }
        return null;
    }

    @Override
    public Session session(boolean create) {
        if (create) {
            String SID = SparkController.webService.createSession();
            session = (SessionImpl) SparkController.webService.getSession(SID);
            return session;
        } else {
            if (headers.containsKey("cookie")) {
                String cookie = headers.get("cookie");
                if (cookie.contains("JSESSIONID=")) {
                    int p = cookie.indexOf("JSESSIONID=");
                    String tmp = cookie.substring(p + "JSESSIONID=".length());
                    p = tmp.indexOf(";");
                    String SID;
                    if (p != -1) {
                        SID = tmp.substring(0, p - 1).trim();
                    } else {
                        SID = tmp.trim();
                    }
                    if (SparkController.webService.isValidSession(SID)) {
                        session = (SessionImpl) SparkController.webService.getSession(SID);
                        return session;
                    }
                    return null;
                }
            }
            return null;
        }
    }

    @Override
    public Map<String, String> params() {
        HashMap<String, String> params = new HashMap<>();
        if (route != null) {
            String routePath = route.getPath();
            String requestPath = this.pathInfo();
            StringTokenizer routeST = new StringTokenizer(routePath, "/");
            StringTokenizer requestST = new StringTokenizer(requestPath, "/");
            while (routeST.hasMoreTokens() && requestST.hasMoreTokens()) {
                String ro = routeST.nextToken();
                String re = requestST.nextToken();
                int sep = ro.indexOf(':');
                if (sep >= 0) {
                    params.put(ro.substring(sep), re);
                }
            }
            return params;
        }
        return null;
    }

    @Override
    public String queryParams(String param) {
        StringBuilder params = new StringBuilder();
        for (String item: parms.get(param)) {
            params.append(item).append(",");
        }
        return params.substring(0, params.length() - 1);
    }

    @Override
    public List<String> queryParamsValues(String param) {
        return parms.getOrDefault(param, null);
    }

    @Override
    public Set<String> queryParams() {
        return parms.keySet();
    }

    @Override
    public String queryString() {
        return this.pre.getOrDefault("queryString", null);
    }

    @Override
    public void attribute(String attrib, Object val) {
        this.attrs.put(attrib, val);
    }

    @Override
    public Object attribute(String attrib) {
        return this.attrs.getOrDefault(attrib, null);
    }

    @Override
    public Set<String> attributes() {
        return this.attrs.keySet();
    }

    @Override
    public Map<String, String> cookies() {
        return null;
    }

    @Override
    public String requestMethod() {
        return this.pre.getOrDefault("method", null);
    }

    @Override
    public String host() {
        return this.socket.getInetAddress().getHostName();
    }

    @Override
    public String userAgent() {
        return this.headers.getOrDefault("user-agent", null);
    }

    @Override
    public int port() {
        return this.socket.getPort();
    }

    @Override
    public String pathInfo() {
        String uri = uri();
        if (uri != null) {
            int p;
            if (uri.startsWith("http://")) {
                p = uri.indexOf(":");
                p += 3;
                try {
                    String tmp = uri.substring(p);
                    p = tmp.indexOf("/");
                    return tmp.substring(p);
                }
                catch (IndexOutOfBoundsException e) {
                    return null;
                }

            } else {
                p = uri.indexOf('/');
            }
            if (p != -1) {
                return uri.substring(p);
            } else {
                return uri;
            }
        }
        return null;
    }

    @Override
    public String url() {
        if (this.pre.containsKey("uri")) {
            String uri = this.pre.get("uri");
            String queryString = pre.get("queryString");
            if (queryString.length() != 0) {
                return uri+"?"+queryString;
            } else {
                return uri;
            }
        }
        return null;
    }

    @Override
    public String uri() {
        return this.pre.getOrDefault("uri", null);
    }

    @Override
    public String protocol() {
        return this.pre.getOrDefault("protocolVersion", "HTTP/1.1");
    }

    @Override
    public String contentType() {
        return this.headers.getOrDefault("content-type", null);
    }

    @Override
    public String ip() {
        return this.socket.getInetAddress().getHostAddress();
    }

    @Override
    public String body() {
        return this.body;
    }

    @Override
    public int contentLength() {
        return Integer.parseInt(this.headers.getOrDefault("content-length", "0"));
    }

    @Override
    public String headers(String name) {
        return this.headers.getOrDefault(name, null);
    }

    @Override
    public Set<String> headers() {
        return this.headers.keySet();
    }
}
