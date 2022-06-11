package edu.upenn.cis.cis455.m2.Route;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class RouteEntry {
    static final Logger logger = LogManager.getLogger(RouteEntry.class);

    String method;
    String path;
    Object handle;

    RouteEntry(String method, String path, Object handle) {
        this.method = method;
        this.path = path;
        this.handle = handle;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Object getHandle() {
        return handle;
    }

    boolean match(String method, String path) {
        if ((method.equals("BEFORE") || method.equals("AFTER")) && (this.method.equals(method)) && (this.path.equals("*"))) {
            return true;
        }
        if (this.method.equals(method)) {
            return pathMatcher(path);
        }
        return false;
    }

    private boolean pathMatcher(String path) {

        if (this.path.equals(path)) {
            // completely match
            return true;
        }

        if (!this.path.endsWith("*") && ((path.endsWith("/") && !this.path.endsWith("/"))
                || (this.path.endsWith("/") && !path.endsWith("/")))) {
            // If not both end with /, must not match
            return false;
        }

        // check params
        ArrayList<String> thisPaths = this.route2list(this.path);
        ArrayList<String> comparePaths = this.route2list(path);

        int thisSize = thisPaths.size();
        int compareSize = comparePaths.size();

        if (thisSize == compareSize) {
            for (int i = 0; i < thisSize; i++) {
                String thisPathPart = thisPaths.get(i);
                String pathPart = comparePaths.get(i);

                if ((i == thisSize - 1) && (thisPathPart.equals("*") && this.path.endsWith("*"))) {
                    // end with wildcard
                    return true;
                }

                if ((!thisPathPart.startsWith(":")) && !thisPathPart.equals("*") && !thisPathPart.equals(pathPart)) {
                    // not equal : * or path, must not match
                    return false;
                }
            }
            // All parts matched
            return true;
        } else {
            // /api/*
            // /api/a/b/c
            if (this.path.endsWith("*")) {
                // /api/a/b/*
                // /api/a/b/
                if (compareSize == (thisSize - 1) && (path.endsWith("/"))) {
                    // for end with slash
                    comparePaths.add(""); comparePaths.add("");
                    compareSize += 2;
                }

                if (thisSize < compareSize) {
                    for (int i = 0; i < thisSize; i++) {
                        String thisPathPart = thisPaths.get(i);
                        String pathPart = comparePaths.get(i);
                        if (thisPathPart.equals("*") && (i == thisSize - 1) && this.path.endsWith("*")) {
                            // end with wildcard
                            return true;
                        }
                        if (!thisPathPart.startsWith(":") && !thisPathPart.equals("*") && !thisPathPart.equals(pathPart)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public ArrayList<String> route2list(String route) {
        String[] paths = route.split("/");
        ArrayList<String> result = new ArrayList<>();
        for (String item : paths) {
            if (item.length() > 0) {
                result.add(item);
            }
        }
        return result;
    }

}
