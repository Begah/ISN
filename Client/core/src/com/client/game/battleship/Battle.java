package com.client.game.battleship;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.core.SceneInput;
import com.client.core.Timer;
import com.client.core.TimerCallback;

public class Battle implements Scene, SceneInput, TimerCallback {
	public Battleship battleship;
	
	Stage stage;
	
	Label label_turnToPlay;
	Table table;
	TextButton confirmButton;
	
	ArrayList<Vector2> bombedCells = new ArrayList<>(), otherBombedCells = new ArrayList<>();
	ArrayList<ParticleEffect> particleEffects = new ArrayList<ParticleEffect>(), otherParticleEffects = new ArrayList<ParticleEffect>();
	
	Vector2 cellSelected = new Vector2(0, 0);
	
	public Battle(Battleship battleship) {
		this.battleship = battleship;
	}
	
	public void updateTurnToPlayLabel(String string) {
		label_turnToPlay.setText(string);
		label_turnToPlay.setPosition(stage.getWidth() / 2.0f - label_turnToPlay.getWidth() / 2.0f, stage.getHeight() - label_turnToPlay.getHeight());
	}

	@Override
	public void init() {
		battleship.currentSceneInput = this;

		for(Ship ship : battleship.ships) {
			MainClass.client.sendString("Battleship Boat Description");
			MainClass.client.sendInt(ship.getType().ordinal());
			MainClass.client.sendInt(ship.x);
			MainClass.client.sendInt(ship.y);
			MainClass.client.sendInt(ship.rotation);
		}
		
		stage = new Stage();
		label_turnToPlay = new Label("aaaaa", battleship.skin);
		stage.addActor(label_turnToPlay);
		
		table = new Table();
		confirmButton = new TextButton("Confirm", battleship.skin);
		confirmButton.setSize(confirmButton.getPrefWidth(), confirmButton.getPrefHeight());
		confirmButton.setPosition(stage.getWidth() - confirmButton.getWidth(), stage.getHeight() / 2.0f - confirmButton.getHeight() / 2.0f);
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cellSelected.isZero())
					return;
				
				for(Vector2 vec : otherBombedCells) {
					if(vec.equals(cellSelected)) // Cell has already been bombed
						return;
				}
				
				otherBombedCells.add(cellSelected.cpy());
				
				Ship s = battleship.shipAt((int)cellSelected.x, (int)cellSelected.y, battleship.turnToPlay ? battleship.otherShips : battleship.ships);
				if(s != null) {
					s.hit((int)cellSelected.x, (int)cellSelected.y);
				}
				
				if(battleship.manager.isServer()) checkWinner();
				
				MainClass.client.sendString("Battleship CellBombed");
				MainClass.client.sendInt((int)cellSelected.x);
				MainClass.client.sendInt((int)cellSelected.y);
				
				ParticleEffect effect = null;
				if(battleship.shipAt((int)cellSelected.x, (int)cellSelected.y, battleship.otherShips) != null)
					effect = battleship.fireEffect.copy();
				else
					effect = battleship.waterEffect.copy();
				effect.init();
				effect.translate(new Vector3(cellSelected.x * Battleship.CellWidth - Battleship.CellWidth / 2.0f, Battleship.WaterTop, cellSelected.y * Battleship.CellHeight - Battleship.CellHeight / 2.0f));
				effect.start();
				battleship.particleSystem.add(effect);
				otherParticleEffects.add(effect);
				
				new Timer(1000).setCallback(Battle.this).setName("EndTurn");
			}
		});
		stage.addActor(confirmButton);
		
		if(battleship.manager.isServer()) {
			final Random rand = new Random();
			if(rand.nextInt(1) == 0) {
				MainClass.client.sendString("Battleship turn to play");
				MainClass.client.sendBool(true);
				battleship.turnToPlay = false;
			} else {
				MainClass.client.sendString("Battleship turn to play");
				MainClass.client.sendBool(false);
				battleship.turnToPlay = true;
			}
		}
	}
	
	public void switchParticles() {
		if(battleship.turnToPlay == true) {
			for(ParticleEffect effect : particleEffects) {
				battleship.particleSystem.remove(effect);
			}
			for(ParticleEffect effect : otherParticleEffects) {
				battleship.particleSystem.add(effect);
			}
		} else {
			for(ParticleEffect effect : otherParticleEffects) {
				battleship.particleSystem.remove(effect);
			}
			for(ParticleEffect effect : particleEffects) {
				battleship.particleSystem.add(effect);
			}
		}
	}
	
	@Override
	public void update() {		
		if(battleship.turnToPlay) {
			updateTurnToPlayLabel("Your turn to play");
			confirmButton.setVisible(true);
		} else {
			updateTurnToPlayLabel(battleship.manager.adversary.username + " turn to play");
			confirmButton.setVisible(false);
		}
		
		stage.act();
	}
	
	public void timerCallback(String name) {
		System.out.println("Timer callback (" + String.valueOf(battleship.manager.isServer()) + ") : " + String.valueOf(battleship.isFinishedPlaying));
		if(battleship.isFinishedPlaying) {
			battleship.manager.gameEnded(battleship.isWinner, !battleship.isWinner);
		}
		
		battleship.turnToPlay = !battleship.turnToPlay;
		
		cellSelected.setZero();
		battleship.water.redstartx = battleship.water.redstartz = 0;
		battleship.water.redendx = battleship.water.redendz = 0;
		
		switchParticles();
	}

	@Override
	public void render() {
		battleship.renderWater();
		battleship.renderScene(battleship.cam, battleship.modelBatch);
		
		stage.draw();
	}

	@Override
	public void dispose() {
		
	}
	
	public void checkWinner() {
		if(battleship.isFinishedPlaying)
			return;
		
		// Check if user is winner
		boolean isWinner = true;
		
		for(int i = 0; i < battleship.otherShips.size(); i++) {//Ship ship : battleship.otherShips) {
			if(battleship.otherShips.get(i).isDestroyed() == false) {
				isWinner = false;
				break;
			}
		}
		if(isWinner) {
			battleship.isFinishedPlaying = true;
			battleship.isWinner = true;
			
			MainClass.client.sendString("Battleship Player won");
			MainClass.client.sendBool(!battleship.isWinner);
			return;
		}
		
		// Check if other user is winner
		isWinner = true;
		for(int i = 0; i < battleship.ships.size(); i++) {//Ship ship : battleship.ships) {
			if(battleship.ships.get(i).isDestroyed() == false) {
				isWinner = false;
				break;
			}
		}
		if(isWinner) {
			battleship.isFinishedPlaying = true;
			battleship.isWinner = false;
			
			MainClass.client.sendString("Battleship Player won");
			MainClass.client.sendBool(!battleship.isWinner);
			return;
		}
	}

	@Override
	public void event(String message) {
		switch(message) {
		case "Battleship CellBombed":
			cellSelected.set(MainClass.client.readInt(), MainClass.client.readInt());
			
			Ship s = battleship.shipAt((int)cellSelected.x, (int)cellSelected.y, battleship.turnToPlay ? battleship.otherShips : battleship.ships);
			if(s != null) {
				s.hit((int)cellSelected.x, (int)cellSelected.y);
			}
			
			if(battleship.manager.isServer()) checkWinner();
			
			battleship.water.redstartx = (cellSelected.x - 1) * Battleship.CellWidth;
			battleship.water.redstartz = (cellSelected.y - 1) * Battleship.CellHeight;
			battleship.water.redendx = (cellSelected.x) * Battleship.CellWidth;
			battleship.water.redendz = (cellSelected.y) * Battleship.CellHeight;
			
			ParticleEffect effect = null;
			if(battleship.shipAt((int)cellSelected.x, (int)cellSelected.y, battleship.ships) != null)
				effect = battleship.fireEffect.copy();
			else
				effect = battleship.waterEffect.copy();
			effect.init();
			effect.translate(new Vector3(cellSelected.x * Battleship.CellWidth - Battleship.CellWidth / 2.0f, Battleship.WaterTop, cellSelected.y * Battleship.CellHeight - Battleship.CellHeight / 2.0f));
			effect.start();
			battleship.particleSystem.add(effect);
			particleEffects.add(effect);
			
			bombedCells.add(cellSelected);
			
			new Timer(1000).setCallback(this).setName("EndTurn");
			break;
		case "Battleship Player won":
			battleship.isWinner = MainClass.client.readBool();
			System.out.println("Client am i winner : " + String.valueOf(battleship.isWinner));
			battleship.isFinishedPlaying = true;
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

	public boolean clicked(float x, float y) {
		if(battleship.turnToPlay == false)
			return false;
		if(stage.hit(x, y, true) != null)
			return true;
		
		Vector3 position = Battleship.RayWaterIntersection(battleship.cam.getPickRay(Gdx.input.getX(),Gdx.input.getY()));
		if(position.x < 0 || position.z < 0 || position.x > Battleship.CellWidth * 10 || position.z > Battleship.CellHeight * 10)
			return false;
		
		int xx = (int)Math.ceil(position.x / Battleship.CellWidth) - 1;
		int yy = (int)Math.ceil(position.z / Battleship.CellHeight) - 1;
		
		battleship.water.redstartx = xx * Battleship.CellWidth;
		battleship.water.redstartz = yy * Battleship.CellHeight;
		battleship.water.redendx = (xx+1) * Battleship.CellWidth;
		battleship.water.redendz = (yy+1) * Battleship.CellHeight;
		
		cellSelected.set(xx + 1, yy + 1);
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		return stage.keyDown(keycode);
	}

	@Override
	public boolean keyUp(int keycode) {
		return stage.keyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		return stage.keyTyped(character);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		return stage.touchDown(x, y, pointer, button);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return stage.touchUp(x, y, pointer, button);
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		return stage.touchDragged(x, y, pointer);
	}

	@Override
	public boolean mouseMoved(int x, int y) {
		return stage.mouseMoved(x, y);
	}

	@Override
	public boolean scrolled(int amount) {
		return stage.scrolled(amount);
	}

}
