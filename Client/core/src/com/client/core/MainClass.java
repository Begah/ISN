package com.client.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.client.menu.LoginMenu;

public class MainClass extends ApplicationAdapter {
	public static Client client = null;
	public final static Random rand = new Random();
	
	private static Scene currentScene;
	private static Scene nextScene = null;
	
	public static ArrayList<Timer> timers = new ArrayList<>();
	
	@Override
	public void create () {		
		currentScene = new LoginMenu();
		currentScene.init();
	}
	
	public static void connectToClient(String username) {
		client = new Client(username);
		client.start();
	}

	@Override
	public void render () {
		if(nextScene != null) {
			currentScene.dispose();
			currentScene = nextScene;
			currentScene.init();
			nextScene = null;
		}
		
		currentScene.update();
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		currentScene.render();
		
		if(!(currentScene instanceof LoginMenu)) {
			try {
				client.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ArrayList<Timer> timers = new ArrayList<>(MainClass.timers);
		for(Timer timer : timers)
			timer.update();
	}
	
	public static void changeScene(Scene newScene) {
		nextScene = newScene;
	}
	
	public static Scene getCurrentScene() {
		return currentScene;
	}
}
