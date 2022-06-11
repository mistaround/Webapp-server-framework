package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stub class for implementing the queue of HttpTasks
 */
public class HttpTaskQueue {
	static final Logger logger = LogManager.getLogger(HttpTaskQueue.class);	
	
	private final ArrayList<HttpTask> taskQueue;
	private final int queueSize;
	private boolean isBeingShutdown = false;

	public HttpTaskQueue(int size) {
		this.taskQueue = new ArrayList<HttpTask>(size);
		this.queueSize = size;
	}
	
	public void add(HttpTask httpTask) {
		while (true) {
			synchronized (taskQueue) {
				if (this.isBeingShutdown) {
					// If shutdown stop adding task
//					logger.debug("Add Being Shutdown!");
					taskQueue.notifyAll();
					break;
				} else {
					if (taskQueue.size() == queueSize) {
//						logger.debug("Queue is full");
						try {
							taskQueue.wait();
						} catch (InterruptedException e) {
							logger.debug("InterruptedException at TaskQueue add");
							break;
						}
					} else {
						taskQueue.add(httpTask);
//						logger.debug("Add new task to Queue");
						taskQueue.notifyAll();
						break;
					}
				}
			}
		}
	}
	
	public HttpTask pop() {
		while (true) {
			synchronized (taskQueue) {
				if (taskQueue.isEmpty()) {
					// If empty and shutdown return null
					if (this.isBeingShutdown) {
//						logger.debug("Pop Being Shutdown!");
						taskQueue.notifyAll();
						return null;
					} else {
//						logger.debug("Queue is currently empty");
						try {
							taskQueue.wait();
						} catch (InterruptedException e) {
							logger.debug("InterruptedException at TaskQueue pop");
							return null;
						}
					}
				} else {
					HttpTask task = taskQueue.remove(0);
//					logger.debug("Notifying everyone we are removing an item");
					taskQueue.notifyAll();
					return task;
				}
			}
		}
	}

	public int size() {
		return taskQueue.size();
	}
	
	public boolean shutdown() {
		this.isBeingShutdown = true;
		return true;
	}
}
