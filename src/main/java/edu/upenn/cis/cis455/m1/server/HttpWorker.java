package edu.upenn.cis.cis455.m1.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.upenn.cis.cis455.m1.handling.ResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.m1.handling.HttpParsing;
import edu.upenn.cis.cis455.SparkController;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m2.server.HttpRequest;
import edu.upenn.cis.cis455.m2.server.HttpResponse;

/**
 * Stub class for a thread worker that handles Web requests
 */
public class HttpWorker implements Runnable {
	
	static final Logger logger = LogManager.getLogger(HttpWorker.class);
	private HttpTask httpTask;
	boolean isBeingShutdown = false;
	
	public HttpTask getTask() {
		return this.httpTask;
	}
	
	public void shutdown() {
    	this.isBeingShutdown = true;
    }
	
    @Override
    public void run() {
    	while (true) {
			HttpTask httpTask;
			// Thread trying to get a task
			httpTask = HttpListener.taskQueue.pop();
			if (httpTask != null) {
				this.httpTask = httpTask;
				Socket socket = httpTask.getSocket();
				BufferedReader br;
				try {
					br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				} catch (IOException e) {
					logger.debug("IOException at HttpWorker");
					throw new HaltException(400);
				}
				// Request parsing
				Map<String, String> pre = new HashMap<>();
				Map<String, List<String>> parms = new HashMap<>();
				Map<String, String> headers = new HashMap<>();
				Map<String, String> body = new HashMap<>();
				if (socket.getInetAddress() != null) {
					headers.put("remote-addr", socket.getInetAddress().toString());
					headers.put("http-client-ip", socket.getInetAddress().toString());
				}
				try {
					HttpParsing.decodeHeader(br, pre, parms, headers, body);
					// Response or Exception
					HttpRequest httpRequest = new HttpRequest(socket, pre, parms, headers, body);
					httpTask.workOnUrl(httpRequest.url());
					if (httpRequest.pathInfo() != null && httpRequest.requestMethod() != null) {
						// Good request should contain host
						if (headers.containsKey("host")) {
							HttpResponse httpResponse = new HttpResponse();
							new ResponseHandler(httpRequest, httpResponse);
							HttpIoHandler.sendResponse(socket, httpRequest, httpResponse);
						} else {
							throw new HaltException(400);
						}
					} else {
						throw new HaltException(400);
					}
					httpTask.finish();
				} catch (HaltException e) {
					HttpIoHandler.sendException(socket, null, e);
				}
			}
			else{
				// if shutdown and null, stop thread
				if (this.isBeingShutdown) {
					logger.debug("Worker Shutdown");
					break;
				}
			}
		}
    }
}
