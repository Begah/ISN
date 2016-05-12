package com.client.game;

import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.game.pendu.Pendu;
import com.client.menu.SocialHub;

public class Manager implements Scene {
	
	private boolean isServer = false;
	public OtherClient adversary;
	
	private Scene currentScene, nextScene;
	
	public boolean wonLastGame;
	private int scorePlayer, scoreOther;
	
	public String gameName = null;
	
	final int ScoreToReach = 3;
	
	public Manager(boolean isServer, OtherClient adversary) {
		this.isServer = isServer;
		this.adversary = adversary;
	}

	@Override
	public void init() {
		this.scorePlayer = 0;
		this.scoreOther = 0;
		
		if(isServer())
			wonLastGame = MainClass.rand.nextBoolean();
		else
			wonLastGame = false;
		
		currentScene = new SelectionScreen(this);
		currentScene.init();
	}

	@Override
	public void update() {
		if(nextScene != null) {
			currentScene.dispose();
			currentScene = nextScene;
			currentScene.init();
			nextScene = null;
			
			gameName = null;
		}

		if(penduString != null && currentScene instanceof Pendu) {
			((Pendu) currentScene).motADeviner = penduString;
			penduString = null;
		}
		
		currentScene.update();
	}

	@Override
	public void render() {		
		currentScene.render();
	}

	@Override
	public void dispose() {
		currentScene.dispose();
	}
	
	public void gameEnded(boolean won, boolean otherWon) {		
		if(isServer) {
			System.out.println("Server : " + String.valueOf(won) + " " + String.valueOf(otherWon));
		} else
			System.out.println("Client : " + String.valueOf(won) + " " + String.valueOf(otherWon));
		
		if(won) {
			scorePlayer++;
			wonLastGame = true;
		} else
			wonLastGame = false;
		if(otherWon) {
			scoreOther++;
			
			if(wonLastGame && !isServer())
				wonLastGame = false;
		}
		
		if(scorePlayer >= ScoreToReach && scoreOther >= ScoreToReach) { // Tie
			changeScene(new FinalScreen(this, false, false, true));
		} else if(scorePlayer >= ScoreToReach) { // Won
			changeScene(new FinalScreen(this, true, false, false));
		} else if(scoreOther >= ScoreToReach) { // Lost
			changeScene(new FinalScreen(this, false, true, false));
		} else
			changeScene(new SelectionScreen(this));
	}

	String penduString = null;
	@Override
	public void event(String message) {
		switch (message) {
		case "Pendu Mot Choisit":
				penduString = MainClass.client.readString();
			break;
		}
		currentScene.event(message);
	}

	@Override
	public void connection(OtherClient user) {
		
	}

	@Override
	public void disconnection(OtherClient user) {
		if(user == adversary) {
			// Player disconnected while in game! :(
			MainClass.changeScene(new SocialHub());
		}
	}

	@Override
	public void clientUpdated(OtherClient user) {
		
	}

	public int getScore() {
		return scorePlayer;
	}
	
	public int getAdverseryScore() {
		return scoreOther;
	}
	
	public void changeScene(Scene nextScene) {
		this.nextScene = nextScene;
	}

	public boolean isServer() {
		return isServer;
	}
}
