package edu.upenn.cis.cis455.m1.handling;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.SparkController;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;

/**
 * Handles marshaling between HTTP Requests and Responses
 */
public class HttpIoHandler {
    final static Logger logger = LogManager.getLogger(HttpIoHandler.class);

    /**
     * Sends an exception back, in the form of an HTTP response code and message.
     * Returns true if we are supposed to keep the connection open (for persistent
     * connections).
     */
    public static boolean sendException(Socket socket, Request request, HaltException except) {
    	try {
    			// Format Date
    			Calendar calendar = Calendar.getInstance();
    		    SimpleDateFormat dateFormat = new SimpleDateFormat(
    		        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    		    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    		    String date = dateFormat.format(calendar.getTime());

    	    	String exceptionHeader = "";
    	    	if(request != null) {
    	    		exceptionHeader += request.protocol();
    	    	} else {
    	    		exceptionHeader += "HTTP/1.1";
    	    	}
    	    	exceptionHeader += " " + except.statusCode();
    	    	// Format Status
    	    	switch (except.statusCode()) {
				case 200:
					exceptionHeader += " " + "OK";
					break;
    	    	case 400:
    				exceptionHeader += " " + "Bad Request";
    				break;
				case 401:
					exceptionHeader += " " + "Unauthorized";
					break;
				case 403:
					exceptionHeader += " " + "Forbidden";
					break;
    			case 404:
    				exceptionHeader += " " + "Not Found";
    				break;
				case 405:
					exceptionHeader += " " + "Method Not Allowed";
					break;
				case 406:
					exceptionHeader += " " + "Not Acceptable";
					break;
				case 407:
					exceptionHeader += " " + "Proxy Authentication Required";
					break;
				case 408:
					exceptionHeader += " " + "Request Timeout";
					break;
				case 409:
					exceptionHeader += " " + "Conflict";
					break;
    			case 500:
    				exceptionHeader += " " + "Internal Server Error";
    				break;
    			case 501:
    				exceptionHeader += " " + "Not Implemented";
				case 502:
					exceptionHeader += " " + "Bad Gateway";
				case 503:
					exceptionHeader += " " + "Service Unavailable";
				case 505:
					exceptionHeader += " " + "HTTP Version Not Supported";
    			default:
    				break;
    			}
    	    	exceptionHeader += "\n";

    	    	exceptionHeader += "Date:";
    	    	exceptionHeader += " " + date;
    	    	exceptionHeader += "\n";

    	    	DataOutputStream dos;
    			dos = new DataOutputStream(socket.getOutputStream());
    			dos.write(exceptionHeader.getBytes());
    			if (except.body() != null) {
					dos.write("\n".getBytes());
					dos.write(except.body().getBytes());
				}
    			dos.close();
    			return true;
		} catch (IOException e) {
			return false;
		}
    }

    /**
     * Sends data back. Returns true if we are supposed to keep the connection open
     * (for persistent connections).
     */
    public static boolean sendResponse(Socket socket, Request request, Response response) {
    	try {
    		// Format Date 
    		Calendar calendar = Calendar.getInstance();
    	    SimpleDateFormat dateFormat = new SimpleDateFormat(
    	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    	    String date = dateFormat.format(calendar.getTime());

    		String responseHeader = "";
    		responseHeader += request.protocol();
    		responseHeader += " " + response.status();
    		
    		switch (response.status()) {
    		case 200:
    			responseHeader += " " + "OK";
    			break;
    		case 201:
				responseHeader += " " + "Created";
    			break;
			case 203:
				responseHeader += " " + "Non-Authoritative Information";
				break;
			case 204:
				responseHeader += " " + "No Content";
				break;
			case 205:
				responseHeader += " " + "Reset Content";
				break;
			case 206:
				responseHeader += " " + "Partial Content";
				break;
			case 300:
				responseHeader += " " + "Multiple Choice";
				break;
			case 301:
				responseHeader += " " + "Moved Permanently";
				break;
			case 302:
				responseHeader += " " + "Found";
				break;
			case 303:
				responseHeader += " " + "See Other";
				break;
			case 304:
				responseHeader += " " + "Not Modified";
				break;
			case 307:
				responseHeader += " " + "Temporary Redirect";
				break;
			case 308:
				responseHeader += " " + "Permanent Redirect";
				break;
			default:
				break;
    		}
    		// Format Header
    		responseHeader += "\n";
    		responseHeader += "Date:";
    		responseHeader += " " + date;
    		responseHeader += "\n";
    		if (response.type() != null) {
        		responseHeader += "Content-Type:";
    			responseHeader += " " + response.type();
        		responseHeader += "\n";
    		}
    		if (response.bodyRaw() != null) {
    			responseHeader += "Content-Length:";
        		responseHeader += " " + response.bodyRaw().length;
        		responseHeader += "\n";
    		}
    		if (response.getHeaders().size() != 0) {
				Map<String,String> headers = response.getHeaders();
				for (Map.Entry<String,String> entry : headers.entrySet()) {
					if (entry.getKey().startsWith("Set-Cookie")) {
						responseHeader += "Set-Cookie:";
					} else {
						responseHeader += entry.getKey() + ":";
					}
					responseHeader += " " + entry.getValue();
					responseHeader += "\n";
				}
			}

    		// If body exists send body
    		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    		dos.write(responseHeader.getBytes());
    		if (!request.requestMethod().equals("HEAD")) {
    			if (response.bodyRaw() != null) {
    				dos.write("\n".getBytes());
    				dos.write(response.bodyRaw());
    			}
    		}
			dos.close();
			// If shutdown, call shutdown
			if (request.requestMethod().equals("GET") && request.pathInfo().equals("/shutdown")) {
            	SparkController.stop();
            }
			return true;
		} catch (IOException e) {
			return false;
		}
    }
}
