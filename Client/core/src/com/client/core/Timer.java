package com.client.core;

import com.badlogic.gdx.Gdx;

public class Timer {
	private long timeLeft;
	private String name;
	
	private TimerCallback callback;
	public boolean isFinished;
	
	public Timer(long timeInSecond) {
		timeLeft = timeInSecond;
		name = null;
		callback = null;
		isFinished = false;
		MainClass.timers.add(this);
	}
	
	public Timer setName(String name) {
		this.name = name;
		return this;
	}
	
	public Timer setCallback(TimerCallback callback) {
		this.callback = callback;
		return this;
	}

	public void update() {
		if(!isFinished) {
			timeLeft -= Gdx.graphics.getDeltaTime() * 1000; // Convert seconds to milliseconds
			if(timeLeft <= 0) {
				isFinished = true;
				if(callback != null)
					callback.timerCallback(name);
				MainClass.timers.remove(this);
			}
		}
	}
}
