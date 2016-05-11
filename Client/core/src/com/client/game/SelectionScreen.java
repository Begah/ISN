package com.client.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.game.battleship.Battleship;
import com.client.game.pendu.Pendu;
import com.client.game.puissance4.Puissance4;

public class SelectionScreen implements Scene {
	
	public Manager manager;
	
	private SpriteBatch batch;
	private Stage stage;
	private Skin skin;
	
	private Table table;
	
	private Label scoreCounter;
	
	private boolean isPicking;
	private boolean showGameChoosing = false;
	
	public SelectionScreen(Manager manager) {
		this.manager = manager;
	}

	@Override
	public void init() {
		isPicking = false;
		
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		
		stage = new Stage();
		
		table = new Table(skin);
		table.setBounds(0, 0, stage.getWidth(), stage.getHeight());
		table.center();
		
		Label lobyName = new Label(MainClass.client.getUsername() + " vs " + manager.adversary.username, skin) {
			Label label1 = null, labelVS = null, label2 = null;
			
			@Override
			public void draw (Batch batch, float parentAlpha) {
				if(label1 == null) {
					label1 = new Label(MainClass.client.getUsername(), skin);
					labelVS = new Label("vs", skin);
					label2 = new Label(manager.adversary.username, skin);
				}
				float characterLengthHalf = scoreCounter.getWidth() / scoreCounter.getText().length / 2;
				label1.setPosition(scoreCounter.getX() + characterLengthHalf - label1.getWidth(), this.getY());
				labelVS.setPosition(scoreCounter.getX() + scoreCounter.getWidth() / 2 - labelVS.getWidth() / 2, this.getY());
				label2.setPosition(scoreCounter.getX() + scoreCounter.getWidth() - characterLengthHalf, this.getY());

				label1.draw(batch, parentAlpha);
				labelVS.draw(batch, parentAlpha);
				label2.draw(batch, parentAlpha);
			}
		};
		table.add(lobyName);
		//lobyName.setX(Gdx.graphics.getWidth() / 2.0f - lobyName.getWidth() / 2.0f);
		table.row();
		scoreCounter = new Label(String.valueOf(manager.getScore()) + "  |  " + String.valueOf(manager.getAdverseryScore()), skin);
		//scoreCounter.setX(Gdx.graphics.getWidth() / 2.0f - scoreCounter.getWidth() / 2.0f);
		table.add(scoreCounter);
		
		table.row();
		
		stage.addActor(table);
		
		if(manager.isServer()) {
			isPicking = manager.wonLastGame;
			MainClass.client.sendString("Pick Game");
			MainClass.client.sendBool(!isPicking); // For other player, it is the inverse of isPicking
			
			if(isPicking)
				setUpPicking();
		}
		
		Gdx.input.setInputProcessor(stage);
	}
	
	ArrayList<Texture> textures = new ArrayList<>();

	private SpriteDrawable imageIcon(String str) {
		Texture texture = new Texture(Gdx.files.internal(str));
		textures.add(texture);
		Sprite sprite = new Sprite(texture);
		sprite.setSize(100, 100);
		return new SpriteDrawable(sprite);
	}
	private void setUpPicking() {
		if(isPicking) {
			table.add(new Label("Pick a game to play : ", skin));
			table.row();
			
			Table table = new Table();
			
			ImageButton button = null;
			table.add(button = new ImageButton(imageIcon("game_icons/battleship.png")));
			button.addListener(new ClickListener(){
	            @Override 
	            public void clicked(InputEvent event, float x, float y){
	            	choseGame("Battleship");
	            }
	        });
			
			table.add(button = new ImageButton(imageIcon("game_icons/hangman.png")));
			button.addListener(new ClickListener(){
	            @Override 
	            public void clicked(InputEvent event, float x, float y){
	            	choseGame("Hangman");
	            }
	        });
			
			table.add(button = new ImageButton(imageIcon("game_icons/poweroffour.png")));
			button.addListener(new ClickListener(){
	            @Override 
	            public void clicked(InputEvent event, float x, float y){
	            	choseGame("PowerOfFour");
	            }
	        });
			
			this.table.add(table);
		} else {
			table.add(new Label("Waiting for other user to pick....", skin));
		}
	}
	
	private void choseGame(String gameName) {
		MainClass.client.sendString("Game Chosen");
		MainClass.client.sendString(gameName);
		
		manager.gameName = gameName;
	}

	@Override
	public void update() {
		stage.act(Gdx.graphics.getDeltaTime());
		
		if(showGameChoosing) {
			showGameChoosing = false;
			setUpPicking();
		}
		
		if(manager.gameName != null) {
			switch(manager.gameName) {
			case "Battleship":
				manager.changeScene(new Battleship(manager));
				break;
			case "Hangman":
				manager.changeScene(new Pendu(manager));
				break;
			case "PowerOfFour":
				manager.changeScene(new Puissance4(manager));
				break;
			}
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
		
		for(int i = 0; i < textures.size(); i++) {
			textures.get(i).dispose();
		}
	}

	@Override
	public void event(String message) {
		switch(message) {
		case "Pick Game":
			this.isPicking = MainClass.client.readBool();
			this.manager.wonLastGame = this.isPicking;
			showGameChoosing = true;
			break;
		case "Game Chosen":
			manager.gameName = MainClass.client.readString();
			break;
		}
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
