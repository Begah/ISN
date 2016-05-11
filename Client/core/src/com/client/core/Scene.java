package com.client.core;

public interface Scene {
	public void init();
	public void update();
	public void render();
	public void dispose();
	
	public void event(String message);
	public void connection(OtherClient user);
	public void disconnection(OtherClient user);
	public void clientUpdated(OtherClient user);
}
