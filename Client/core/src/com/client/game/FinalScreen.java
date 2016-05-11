package com.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.core.Timer;
import com.client.core.TimerCallback;
import com.client.menu.SocialHub;

public class FinalScreen implements Scene {

	public Manager manager;
	
	private SpriteBatch batch;
	private Stage stage;
	private Skin skin;
	
	boolean won, lost, tie;
	
	public FinalScreen(Manager manager, boolean won, boolean lost, boolean tie) {
		this.manager = manager;
		this.won = won;
		this.lost = lost;
		this.tie = tie;
	}
	
	@Override
	public void init() {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		
		stage = new Stage();
		
		Label label = null;
		if(won)
			label = new Label("You won!", skin);
		else if(lost)
			label = new Label("You lost!", skin);
		else if(tie)
			label = new Label("It is a tie!", skin);
		
		label.setSize(label.getPrefWidth(), label.getPrefHeight());
		label.setPosition(stage.getWidth() / 2.0f - label.getWidth() / 2.0f, stage.getHeight() / 2.0f - label.getHeight() / 2.0f);
		stage.addActor(label);
		
		new Timer(4000).setCallback(new TimerCallback() {
			public void timerCallback(String name) {
				MainClass.client.sendString("Update Playing Status");
				MainClass.client.sendBool(false);
				MainClass.changeScene(new SocialHub());
			}
		});
	}
	
	@Override
	public void update() {
		stage.act(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void render() {
		batch.begin();
        stage.draw();
        batch.end();
	}
	
	@Override
	public void dispose() {
		stage.dispose();
		batch.dispose();
		skin.dispose();
	}
	
	@Override
	public void event(String message) {
	}
	
	@Override
	public void connection(OtherClient user) {
		
	}
	
	@Override
	public void disconnection(OtherClient user) {

	}
	
	@Override
	public void clientUpdated(OtherClient user) {

	}
}
