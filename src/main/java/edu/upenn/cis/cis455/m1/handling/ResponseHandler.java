package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.SparkController;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.server.HttpListener;
import edu.upenn.cis.cis455.m1.server.WebService;
import edu.upenn.cis.cis455.m2.Route.FilterImpl;
import edu.upenn.cis.cis455.m2.Route.RouteEntry;
import edu.upenn.cis.cis455.m2.server.HttpRequest;
import edu.upenn.cis.cis455.m2.server.HttpResponse;
import edu.upenn.cis.cis455.m2.Route.RouteImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class ResponseHandler {
    static final Logger logger = LogManager.getLogger(ResponseHandler.class);

    private final String port = WebService.port == 0 ? "45555" : Integer.toString(WebService.port);
    private final String ipAddress = WebService.ipAddress == null ? "0.0.0.0" : WebService.ipAddress;

    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    public ResponseHandler(HttpRequest httpRequest, HttpResponse httpResponse) throws HaltException {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        try {
            buildResponse();
        } catch (HaltException e) {
            throw new HaltException(e.statusCode(), e.body());
        }
    }

    public void buildResponse() throws HaltException {
        try {
            String path = this.httpRequest.pathInfo();
            ArrayList<RouteEntry> routes = SparkController.webService.getRoutes(this.httpRequest.requestMethod(), path);
            applyFilter("BEFORE", path);
            if (routes.size() != 0) {
                logger.debug("Use Dynamic Handler");
                dynamicHandler(routes);
            } else {
                logger.debug("Use Static Handler");
                staticHandler();
            }
            applyFilter("AFTER", path);
            if (httpRequest.session != null) {
                httpResponse.cookie("JSESSIONID", httpRequest.session.id());
            }
        } catch (HaltException e) {
            throw new HaltException(e.statusCode(), e.body());
        }
    }

    public void applyFilter(String method, String path) throws HaltException {
        try {
            ArrayList<RouteEntry> filters = SparkController.webService.getFilters(method, path);
            for (RouteEntry item: filters) {
                FilterImpl filter = new FilterImpl(item.getPath(), item.getHandle());
                filter.handle(httpRequest, httpResponse);
                logger.info("Apply Filter: " + method + " " + path);
            }
        } catch (HaltException e) {
            logger.debug("User Exception In applyBefore");
            throw new HaltException(e.statusCode(), e.body());
        } catch (Exception e) {
            logger.debug("Internal Exception In applyBefore");
            throw new HaltException(500);
        }
    }

    public void dynamicHandler(ArrayList<RouteEntry> routes) throws HaltException {
        try {
            RouteEntry item = routes.get(0);
            RouteImpl route = new RouteImpl(item.getPath(), item.getHandle());
            httpRequest.workOnRoute(route);
            Object obj = route.handle(httpRequest, httpResponse);
            if (this.httpRequest.requestMethod().equals("POST") ||
                this.httpRequest.requestMethod().equals("PUT") ||
                this.httpRequest.requestMethod().equals("DELETE")) {
                this.httpResponse.status(201);
            } else {
                this.httpResponse.status(200);
            }
            this.httpResponse.body(obj.toString());
            if (this.httpResponse.type() == null) {
                this.httpResponse.type("text/html");
            }
        } catch (HaltException e) {
            logger.debug("User Exception In dynamicHandler");
            throw new HaltException(e.statusCode(), e.body());
        } catch (Exception e) {
            logger.debug("Internal Exception In dynamicHandler");
            throw new HaltException(500);
        }

    }

    public void staticHandler() throws HaltException {
        String uri = httpRequest.uri();
        if (uri.startsWith("https://")) {
            throw new HaltException(505);
        }
        if (uri.startsWith("http://")) {
            int p = uri.indexOf(":");
            p += 3;
            try {
                String tmp = uri.substring(p);
                p = tmp.indexOf("/");
                if (p != -1) {
                    tmp = tmp.substring(0,p);
                }
                if (!tmp.equals("localhost:" + this.port) && !tmp.equals(this.ipAddress + ":" + this.port)) {
                    throw new HaltException(404);
                }
            }
            catch (IndexOutOfBoundsException e) {
                throw new HaltException(400);
            }
        }
        String method = this.httpRequest.requestMethod();
        if (method.equals("GET") || method.equals("HEAD")) {
            // Can't figure out a proper place to put control, may be changed later
            if (this.httpRequest.pathInfo().equals("/control")) {
                String html = "<!DOCTYPE html>\n"
                        + "<html>\n"
                        + "<head>\n"
                        + "    <title>Control Panel</title>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "<h1>Control Panel</h1>\n"
                        + "<ul>\n";
                String info = HttpListener.threadPool.getInfo();
                String[] infos = info.split("\n");
                for (int i = 0; i < infos.length; i++) {
                    html += "<li>Thread" + i + " " + infos[i] + "</li>\n";
                }
                html += "<li><a href=\"/shutdown\">Shut down</a></li>\n"
                        + "</ul>\n"
                        + "</body>\n"
                        + "</html>";
                this.httpResponse.status(200);
                this.httpResponse.body(html);
                this.httpResponse.type("text/html");
            } else if (this.httpRequest.pathInfo().equals("/shutdown")) {
                this.httpResponse.status(200);
            } else {
                String pathname = this.httpRequest.pathInfo();
                try {
                    FileRequestHandler handler = new FileRequestHandler(pathname,method);
                    byte[] content = handler.getFileContent();
                    int statusCode = handler.getStatusCode();
                    String contentType = handler.getContentType();
                    this.httpResponse.status(statusCode);
                    this.httpResponse.bodyRaw(content);
                    if (!handler.getContentType().equals("")) {
                        this.httpResponse.type(contentType);
                    }
                } catch (HaltException e) {
                    throw new HaltException(e.statusCode());
                }
            }
        } else {
            throw new HaltException(501);
        }
    }
}
