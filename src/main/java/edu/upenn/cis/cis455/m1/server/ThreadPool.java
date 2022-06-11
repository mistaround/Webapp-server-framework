package edu.upenn.cis.cis455.m1.server;

import java.util.ArrayList;
import java.util.Iterator;

public class ThreadPool {
	
	private ArrayList<Thread> threadPool;
	private ArrayList<HttpWorker> workers;
	
	public ThreadPool(int threads) {
		threadPool = new ArrayList<Thread>(threads);
		workers = new ArrayList<HttpWorker>(threads);
    	for (int i = 0; i < threads; i += 1) {
    		HttpWorker httpWorker = new HttpWorker();
    		workers.add(httpWorker);
    		threadPool.add(new Thread(httpWorker));
    		threadPool.get(i).start();
    	}
	}
	
	public String getInfo() {
		String info = "";
		for(Iterator<HttpWorker> it = workers.iterator(); it.hasNext();) {
			HttpTask task = it.next().getTask();
			if (task == null) {
				info += "Waiting" + "\n";
			} else {
				info += task.getInfo() + "\n";
			}
			
		}
		return info;
	}
	
	public void shutdown() {
		for(Iterator<HttpWorker> it = workers.iterator(); it.hasNext();) {
			it.next().shutdown();
		}
	}

}
