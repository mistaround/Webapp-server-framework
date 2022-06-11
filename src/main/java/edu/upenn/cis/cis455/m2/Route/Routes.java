package edu.upenn.cis.cis455.m2.Route;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class Routes {
    static final Logger logger = LogManager.getLogger(Routes.class);
    private final ArrayList<RouteEntry> routes;

    public Routes() {
        routes = new ArrayList<>();
    }

    public void add(String method, String path, Object target) {
        RouteEntry entry = new RouteEntry(method, path, target);
        logger.info("Adds route: " + method + " " + path);
        routes.add(entry);
    }

    public ArrayList<RouteEntry> getRoutes(String method, String path) {
        ArrayList<RouteEntry> matches = new ArrayList<RouteEntry>();
        for (RouteEntry item : routes) {
            if (item.getMethod().equals(method)) {
                if (item.match(method, path)) {
                    matches.add(item);
                }
            }
        }
        return matches;
    }

    public ArrayList<RouteEntry> getFilters(String method, String path) {
        ArrayList<RouteEntry> matches = new ArrayList<RouteEntry>();
        for (RouteEntry item : routes) {
            if (item.getMethod().equals(method)) {
                if (item.match(method, path)) {
                    matches.add(item);
                }
            }
        }
        return matches;
    }
}
