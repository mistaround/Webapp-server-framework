/**
 * CIS 455/555 route-based HTTP framework
 * 
 * Z. Ives, 8/2017
 * 
 * Portions excerpted from or inspired by Spark Framework, 
 * 
 *                 http://sparkjava.com,
 * 
 * with license notice included below.
 */

/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.m2.Route.FilterImpl;
import edu.upenn.cis.cis455.m2.Route.RouteEntry;
import edu.upenn.cis.cis455.m2.Route.RouteImpl;
import edu.upenn.cis.cis455.m2.Route.Routes;
import edu.upenn.cis.cis455.m2.Session.Sessions;
import edu.upenn.cis.cis455.m2.interfaces.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.m2.interfaces.Route;
import edu.upenn.cis.cis455.m2.interfaces.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebService extends edu.upenn.cis.cis455.m1.server.WebService {
    final static Logger logger = LogManager.getLogger(WebService.class);
    public boolean start = false;
    Routes routes = new Routes();
    Sessions sessions = new Sessions();


    public WebService() {
        super();
    }

    ///////////////////////////////////////////////////
    // For more advanced capabilities
    ///////////////////////////////////////////////////

    private void registerRoute(String method, String path, RouteImpl route) {
        if (!start) {
            start = true;
            this.start();
        }
        routes.add(method, path, route);
    }

    private void registerFilter(String method, String path, FilterImpl filter) {
        if (!start) {
            start = true;
            this.start();
        }
        routes.add(method, path, filter);
    }

    public ArrayList<RouteEntry> getRoutes(String method, String path) {
        return routes.getRoutes(method, path);
    }

    public ArrayList<RouteEntry> getFilters(String method, String path) {
        return routes.getFilters(method, path);
    }

    public String createSession() {
        return sessions.create();
    }

    public Session getSession(String id) {
        Session session = sessions.get(id);
        if (session != null) {
            if (sessions.isValid(id)) {
                return session;
            }
        }
        return null;
    }

    public boolean isValidSession(String id) {
        return sessions.isValid(id);
    }

    /**
     * Handle an HTTP GET request to the path
     */
    public void get(String path, Route route) {
        registerRoute("GET", path, new RouteImpl(path, route));
    }

    /**
     * Handle an HTTP POST request to the path
     */
    public void post(String path, Route route) {
        registerRoute("POST", path, new RouteImpl(path, route));
    }

    /**
     * Handle an HTTP PUT request to the path
     */
    public void put(String path, Route route) {
        registerRoute("PUT", path, new RouteImpl(path, route));
    }

    /**
     * Handle an HTTP DELETE request to the path
     */
    public void delete(String path, Route route) {
        registerRoute("DELETE", path, new RouteImpl(path, route));
    }

    /**
     * Handle an HTTP HEAD request to the path
     */
    public void head(String path, Route route) {
        registerRoute("HEAD", path, new RouteImpl(path, route));
    }

    /**
     * Handle an HTTP OPTIONS request to the path
     */
    public void options(String path, Route route) {
        registerRoute("OPTIONS", path, new RouteImpl(path, route));
    }

    ///////////////////////////////////////////////////
    // HTTP request filtering
    ///////////////////////////////////////////////////

    /**
     * Add filters that get called before a request
     */
    public void before(Filter filter) {
        registerFilter("BEFORE", "/*", new FilterImpl(null, filter));
    }

    /**
     * Add filters that get called after a request
     */
    public void after(Filter filter) {
        registerFilter("AFTER", "/*", new FilterImpl(null, filter));
    }

    /**
     * Add filters that get called before a request
     */
    public void before(String path, Filter filter) {
        registerFilter("BEFORE", path, new FilterImpl(path, filter));
    }

    /**
     * Add filters that get called after a request
     */
    public void after(String path, Filter filter) {
        registerFilter("AFTER", path, new FilterImpl(path, filter));
    }

}
