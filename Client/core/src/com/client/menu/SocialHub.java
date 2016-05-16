package com.client.menu;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.client.core.Dialog;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.game.Manager;

public class SocialHub implements Scene {
	private Stage stage;
	private Skin skin;
	private SpriteBatch batch;
	
	private Window clientOnline;
	public ArrayList<ClientButton> clientButtons = new ArrayList<ClientButton>();

	private ClientButton userSelected, gameRequestClient = null;
	private boolean askedForGame = false;
	
	private Dialog dialog;
	private TextButton dialogSend, dialogCancel;
	private Label userLabel, messageLabel;
	
	private com.badlogic.gdx.scenes.scene2d.ui.Dialog gameRequest;
	private Label gameRequestLabel;
	
	private TextButtonStyle WhiteText, RedText;

	private boolean hasBeenInited = false;
	
	public boolean StagehasActor(Actor actor) {
		for(int i = 0; i < stage.getActors().size; i++) {
			if(stage.getActors().get(i) == actor)
				return true;
		}
		return false;
	}
	
	private class ClientButton extends TextButton {
		public OtherClient user;
		private ClientButton self;
		
		public ClientButton(final OtherClient user) {
			super(user.username, skin);
			this.self = this;
			this.user = user;
			
			configure();

			if(clientOnline == null) {
				System.err.println("WTF clientonline null");
			}
			
			clientOnline.add(this).minWidth(clientOnline.getWidth() - clientOnline.getPadLeft() - clientOnline.getPadRight());
			clientOnline.row();
			
			this.addListener(new ClickListener() {
				@Override 
	            public void clicked(InputEvent event, float x, float y){
					if(askedForGame == true)
						return;
					
					dialog.setVisible(true);
	            	userLabel.setText("User : " + user.username);
	            	userSelected = self;
	            	dialog.setVisible(true);
	            	messageLabel.setVisible(false);
	            	
	            	batch.begin();
	            	dialog.draw(batch, 0);
	            	batch.end();
	            }
			});
		}
		
		public void configure() {
			if(user.isPlaying)
				setStyle(RedText);
			else
				setStyle(WhiteText);
			super.setText(user.username);
		}
	}
	
	@Override
	public void init() {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		
		stage = new Stage();
		
		WhiteText = new TextButtonStyle(skin.get(TextButtonStyle.class));
		RedText = new TextButtonStyle(WhiteText);
		RedText.fontColor = Color.RED;
		
		clientOnline = new Window("Users Online", skin);
		clientOnline.align(Align.topLeft);
		clientOnline.setHeight(stage.getHeight());
		
		stage.addActor(clientOnline);
		
		// Dialog to send game request to users
		dialog = new Dialog("Send invitation request", skin);
		
		userLabel = dialog.addText("User : WWWWWWW");
		dialog.row();
		
		dialogSend = dialog.addButton("Send");
		dialogSend.addListener(new ClickListener() {
			@Override 
            public void clicked(InputEvent event, float x, float y){
				if(askedForGame) return;
				
				if(userSelected.user.isPlaying == true) {
					messageLabel.setText("User is already in game");
					messageLabel.setVisible(true);
				} else {
					dialogSend.setDisabled(true);
					dialogCancel.setDisabled(true);
					messageLabel.setVisible(true);
					messageLabel.setText("Sending request to user");
					askedForGame = true;
					
					MainClass.client.sendString("Game Request");
					MainClass.client.sendInt(userSelected.user.ID);
				}
				
				batch.begin();
            	dialog.draw(batch, 0);
            	batch.end();
            }
		});
		
		dialogCancel = dialog.addButton("Cancel");
		dialogCancel.addListener(new ClickListener() {
			@Override 
            public void clicked(InputEvent event, float x, float y){
				if(askedForGame)
					return;
				
				dialog.setVisible(false);
				messageLabel.setVisible(false);
            }
		});
		
		dialog.row();
		
		messageLabel = dialog.addText("");
		LabelStyle ls = new LabelStyle(messageLabel.getStyle());
		ls.fontColor = Color.RED;
		messageLabel.setStyle(ls);
		messageLabel.setVisible(false);
		
		dialog.show(stage);
		dialog.setVisible(false);
		
		// Dialog when asked to play
		gameRequest = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("Game Request", skin) {
			@Override
			public void result(Object obj) {
				int num = (int)obj;
				
				if(num == 0) {
					//Accept
					MainClass.client.sendString("Game Request Answer");
					MainClass.client.sendInt(gameRequestClient.user.ID);
					MainClass.client.sendBool(true);
					
					moveToGameManager(false, gameRequestClient.user);
				} else {
					MainClass.client.sendString("Game Request Answer");
					MainClass.client.sendInt(gameRequestClient.user.ID);
					MainClass.client.sendBool(false);
					gameRequestClient = null;
				}
			}
		};
		
		gameRequestLabel = new Label("User XXXX wants to play with you", skin);
		gameRequest.text(gameRequestLabel);
		
		gameRequest.button("Accept", 0);
		gameRequest.button("Refuse", 1);
		
		Gdx.input.setInputProcessor(stage);
		
		for(int i = 0; i < MainClass.client.otherClients.size(); i++) {
			System.out.println("Client addition in init : " + MainClass.client.otherClients.get(i).username);
			clientButtons.add(new ClientButton(MainClass.client.otherClients.get(i)));
		}

		hasBeenInited = true;
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
		switch(message) {
		case "Game Request":
			int oClientID = MainClass.client.readInt();
			
			if(askedForGame == true) { // Asked somoene for a game, cannot be asked
				MainClass.client.sendString("Game Request Answer");
				MainClass.client.sendInt(oClientID);
				MainClass.client.sendBool(false);
			} else {
				for(int i = 0; i < clientButtons.size(); i++) {
					if(clientButtons.get(i).user.ID == oClientID) {
						gameRequestClient = clientButtons.get(i);
						gameRequestLabel.setText("User " + gameRequestClient.getText() + " wants to play");
					}
				}
				gameRequest.show(stage);
			}
			break;
		case "Game Request Answer":
			boolean response = MainClass.client.readBool();
			
			if(response == true) {
				// Start to play game :)
				moveToGameManager(true, userSelected.user);
			} else {
				askedForGame = false;
				messageLabel.setText("User refused to play");
			}
			break;
		}
	}
	
	@Override
	public void connection(OtherClient user) {
		if(hasBeenInited == false) return;
		System.out.println("Adding connection of user : " + user.username);
		clientButtons.add(new ClientButton(user));
	}
	
	@Override
	public void disconnection(OtherClient user) {
		for(int i = 0; i < clientButtons.size(); i++) {
			if(clientButtons.get(i).user == user) {
				if(userSelected == clientButtons.get(i))
					dialog.setVisible(false);
				
				clientOnline.removeActor(clientButtons.get(i));
				clientButtons.remove(i);
				break;
			}
		}
	}
	
	public void moveToGameManager(boolean isServer, OtherClient user) {
		MainClass.changeScene(new Manager(isServer, user));
		
		MainClass.client.sendString("Update Playing Status");
		MainClass.client.sendBool(true);
	}

	@Override
	public void clientUpdated(OtherClient user) {
		for(int i = 0; i < clientButtons.size(); i++) {
			if(clientButtons.get(i).user == user) {
				clientButtons.get(i).configure();
			}
		}
	}

}
