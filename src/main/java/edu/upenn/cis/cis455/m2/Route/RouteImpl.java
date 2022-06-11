package edu.upenn.cis.cis455.m2.Route;

import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import edu.upenn.cis.cis455.m2.interfaces.Route;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouteImpl implements Route {
    static final Logger logger = LogManager.getLogger(RouteImpl.class);

    public final String path;
    public final Route route;

    public RouteImpl(String path, Route route) {
        this.path = path;
        this.route = route;
    }

    public RouteImpl(String path, Object route) {
        this.path = path;
        this.route = (Route) route;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        logger.info("Handle Route: " + this.path);
        return this.route.handle(request, response);
    }

    public String getPath() {
        return this.path;
    }
}
