package com.client.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.core.Client;

public class LoginMenu implements Scene {
	private Stage stage;
	private Skin skin;
	private SpriteBatch batch;
	
	private TextField textfield;
	private Label label, message;
	private TextButton button;

	@Override
	public void init() {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		
		stage = new Stage();
		
		// Username textfield in center of screen
		textfield = new TextField("", skin);
        
        textfield.setWidth(Gdx.graphics.getWidth() / 2);
        textfield.setPosition(Gdx.graphics.getWidth() / 2 - textfield.getWidth() / 2, Gdx.graphics.getHeight() / 2 - textfield.getHeight() / 2);
        textfield.setMaxLength(12);
        
        stage.addActor(textfield);
        
        // Username label in front of textfield
        label  = new Label("Username : ", skin);
        
        label.setWidth(label.getPrefWidth());
        label.setHeight(textfield.getHeight());
        label.setPosition(textfield.getX() - label.getWidth(), textfield.getY() + textfield.getHeight() / 2 - label.getHeight() / 2);
        
        stage.addActor(label);
        
        // Possible error message under textfield
        message = new Label("", skin);
        
        message.setWidth(message.getPrefWidth());
        message.setHeight(message.getPrefHeight());
        message.setPosition(textfield.getX() + textfield.getWidth() / 2 - message.getWidth() / 2, textfield.getY() - message.getHeight() / 2 - message.getHeight() / 2);
        
        stage.addActor(message);
		
        // Login button after textfield
        button = new TextButton("Login", skin, "default");
        
        button.setWidth(100);
        button.setPosition(textfield.getX() + textfield.getWidth() + 10, textfield.getY() + textfield.getHeight() / 2 - button.getHeight() / 2);
        
        button.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	button.setDisabled(true);
            	textfield.setDisabled(true);
            	
            	message.setText("Waiting to connect...");
            	message.setWidth(message.getPrefWidth());
            	message.setHeight(message.getPrefHeight());
            	message.setPosition(textfield.getX() + textfield.getWidth() / 2 - message.getWidth() / 2, textfield.getY() - message.getHeight() / 2 - message.getHeight() / 2);
            	MainClass.connectToClient(textfield.getText());
            }
        });
        
        stage.addActor(button);
        
        Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void update() {
		stage.act(Gdx.graphics.getDeltaTime());
		
		if(MainClass.client != null && MainClass.client.state == Client.State.FailedToConnect) {
			button.setDisabled(false);
			textfield.setDisabled(false);
			
			message.setText("Failed to connect to server");
            message.setWidth(message.getPrefWidth());
            message.setHeight(message.getPrefHeight());
            message.setPosition(textfield.getX() + textfield.getWidth() / 2 - message.getWidth() / 2, textfield.getY() - message.getHeight() / 2 - message.getHeight() / 2);
		}
		if(MainClass.client != null && MainClass.client.state == Client.State.Connected) {
			MainClass.changeScene(new SocialHub());
		}
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
