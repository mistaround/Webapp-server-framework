package edu.upenn.cis.cis455.m2.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;


public class Sessions {
    static final Logger logger = LogManager.getLogger(Sessions.class);
    private final HashMap<String, SessionImpl> sessions = new HashMap<>();

    public Sessions() {}

    public String create() {
        String id = String.valueOf(new Random().nextLong());
        while (sessions.containsKey(id)) {
            id = String.valueOf(new Random().nextLong());
        }
        SessionImpl session = new SessionImpl(id);
        sessions.put(id, session);
        logger.info("Session Created: " + id);
        return id;
    }

    public SessionImpl get(String id) {
        SessionImpl session = sessions.getOrDefault(id, null);
        session.access();
        return session;
    }

    public boolean isValid(String id) {
        SessionImpl session = this.get(id);
        if (session != null) {
            if (session.maxInactiveInterval() < (new Date().getTime() - session.creationTime())) {
                session.invalidate();
                return false;
            }
            return true;
        }
        return false;
    }
}
