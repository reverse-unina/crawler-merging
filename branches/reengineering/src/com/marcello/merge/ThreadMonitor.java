package com.marcello.merge;

import java.util.Observable;

public class ThreadMonitor extends Observable implements Runnable{

	Thread[] threads;


	public ThreadMonitor(Thread[] threads) {
		super();
		this.threads = threads;
	}

	@Override
	public void run() {
		for(int i=0; i<this.threads.length; i++){
			try {
				this.threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(this.countObservers()!=0){
			this.setChanged();
			this.notifyObservers();
		}
		System.out.println("monitor: notification sent");
	}

}
