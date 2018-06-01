package com.harman.rtnm.samsung.commonutils.changelistener;

import java.io.File;


public class DirChangeListener implements Runnable {

	private ChangeListener<File> changeListener = null;
	private volatile boolean running = false;
	
	public DirChangeListener(ChangeListener<File> listener) {
		if(listener == null) {
			throw new NullPointerException( " change listener is null");
		}
		
		this.changeListener = listener;
	}

	@Override
	public void run() {
		this.running = true;
	}

	
	public void start() {		
		if(this.running) {
			throw new IllegalStateException(" Directory change listener is already running...");
		}
		
		new Thread(this).start();
	}

	public void stop() {
		this.running = false;
	}
}
