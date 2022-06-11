package edu.upenn.cis.cis455.m1.server;

import java.net.Socket;

public class HttpTask {
	HttpTaskQueue taskQueue;
    Socket requestSocket;
    // Utils for /control
    boolean idle = true;
    String url= "";

    public HttpTask(Socket socket, HttpTaskQueue taskQueue) {
        this.requestSocket = socket;
        this.taskQueue = taskQueue;
    }

    public Socket getSocket() {
        return requestSocket;
    }
    // To change idle status and store url
    public void workOnUrl(String url) {
    	this.idle = false;
    	this.url = url;
    }
    
    public String url() {
    	return this.url;
    }
    // To change idle status and clear url
    public void finish() {
    	this.idle = true;
    	this.url = "";
    }
    
    public boolean idle() {
    	return this.idle();
    }
    // Util for /control
    public String getInfo() {
    	if (this.idle == true) {
    		return "Waiting";
    	} else {
    		return "Handling " + this.url;
    	}
    }
    
}
