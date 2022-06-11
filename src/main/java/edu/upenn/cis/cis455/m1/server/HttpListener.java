package edu.upenn.cis.cis455.m1.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stub for your HTTP server, which listens on a ServerSocket and handles
 * requests
 */
public class HttpListener implements Runnable {
	
	static final Logger logger = LogManager.getLogger(HttpListener.class);	
	// Server should be unique so declare as static
	public static ServerSocket serverSocket;
	public static HttpTaskQueue taskQueue;
	public static ThreadPool threadPool;
	boolean isBeingShutdown = false;
	int queueSize = 8;
	
	public HttpListener(int port, int poolSize) {
		try {
			// Create server
			serverSocket = new ServerSocket(port);
			taskQueue = new HttpTaskQueue(queueSize);
			threadPool = new ThreadPool(poolSize);
			logger.debug("Serversocket on port: " + port);
			logger.debug("Create TaskQueue of size: " + queueSize);
			logger.debug("Create ThreadPool of size: " + poolSize);
		} catch (IOException e) {
			logger.debug("???");
		}
	}
	public boolean shutdown() {
		threadPool.shutdown();
		boolean ready = taskQueue.shutdown();
		if (ready) {
			this.isBeingShutdown = true;
			try {
				serverSocket.close();
				return true;
			} catch (IOException e) {
				logger.debug("shutdown???");
			}
		}
		return false;
	}
	
    @Override
    public void run() {
        while(true) {
        	if (!this.isBeingShutdown) {
        		try {
        			// If shutdown, stop socket server
					Socket socket = serverSocket.accept();
    				logger.debug("New Client: "+ socket.getInetAddress().getLocalHost() + " connected");
    				HttpTask httpTask = new HttpTask(socket, taskQueue);
					taskQueue.add(httpTask);
				} catch (IOException e) {
    				break;
    			}
        	} else {
        		logger.debug("Being Shutdown Refuse Incoming Socket");
        		break;
        	}
        }
    }
}
