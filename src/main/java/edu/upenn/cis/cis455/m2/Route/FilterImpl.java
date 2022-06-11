package edu.upenn.cis.cis455.m2.Route;

import edu.upenn.cis.cis455.m2.interfaces.Filter;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilterImpl implements Filter {
    static final Logger logger = LogManager.getLogger(FilterImpl.class);

    private final String path;
    private final Filter filter;

    public FilterImpl(String path, Filter filter) {
        this.filter = filter;
        this.path = path;
    }

    public FilterImpl(String path, Object filter) {
        this.filter = (Filter) filter;
        this.path = path;
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        this.filter.handle(request, response);
    }

    public String getPath() {
        return this.path;
    }

}
