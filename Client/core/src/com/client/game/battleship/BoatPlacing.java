package com.client.game.battleship;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.core.SceneInput;
import com.client.game.battleship.Ship.Type;

public class BoatPlacing implements Scene, SceneInput {
	
	Battleship battleship;
	Stage stage;
	Label stageName;
	
	Window shipWindow;
	Table confirmTable;
	TextButton ACbutton, Battleshipbutton, Cruiserbutton, Destroyerbutton, Submarinebutton;
	TextButton PlaceButton, RemoveButton, ConfirmButton;
	ButtonGroup<TextButton> shipGroup, actionGroup;
	
	boolean isOtherDone = false, isDone = false;

	public BoatPlacing(Battleship battleship) {
		this.battleship = battleship;
	}

	@Override
	public void init() {
		battleship.currentSceneInput = this;

		stage = new Stage();
		stageName = new Label("Please place your boats", battleship.skin);
		stageName.setX(Gdx.graphics.getWidth() / 2 - stageName.getPrefWidth() / 2);
		stageName.setY(Gdx.graphics.getHeight() - stageName.getHeight());
		stage.addActor(stageName);
		
		shipWindow = new Window("Ships", battleship.skin);
		
		actionGroup = new ButtonGroup<>();
		
		PlaceButton = new TextButton("Place", battleship.skin, "toggle");
		RemoveButton = new TextButton("Remove", battleship.skin, "toggle");
		
		actionGroup.add(PlaceButton, RemoveButton);
		Table table = new Table();
		table.add(PlaceButton, RemoveButton);
		shipWindow.add(table);
		shipWindow.row();
		
		shipGroup = new ButtonGroup<>();
		
		ACbutton = new TextButton("Aircraft Carrier", battleship.skin, "toggle");
		Battleshipbutton = new TextButton("Battleship", battleship.skin, "toggle");
		Cruiserbutton = new TextButton("Cruiser", battleship.skin, "toggle");
		Destroyerbutton = new TextButton("Destroyer", battleship.skin, "toggle");
		Submarinebutton = new TextButton("Submarine", battleship.skin, "toggle");
		shipGroup.add(ACbutton, Battleshipbutton, Cruiserbutton, Destroyerbutton, Submarinebutton);
		
		table = new Table();
		Label label = null;
		table.add(label = new Label("1", battleship.skin));
		table.add(ACbutton).minWidth(shipWindow.getWidth() - shipWindow.getPadLeft() - shipWindow.getPadRight() - label.getWidth());
		shipWindow.add(table);shipWindow.row();
		
		table = new Table();
		table.add(label = new Label("1", battleship.skin));
		table.add(Battleshipbutton).minWidth(shipWindow.getWidth() - shipWindow.getPadLeft() - shipWindow.getPadRight() - label.getWidth());
		shipWindow.add(table);shipWindow.row();
		
		table = new Table();
		table.add(label = new Label("1", battleship.skin));
		table.add(Cruiserbutton).minWidth(shipWindow.getWidth() - shipWindow.getPadLeft() - shipWindow.getPadRight() - label.getWidth());
		shipWindow.add(table);shipWindow.row();
		
		table = new Table();
		table.add(label = new Label("2", battleship.skin));
		table.add(Destroyerbutton).minWidth(shipWindow.getWidth() - shipWindow.getPadLeft() - shipWindow.getPadRight() - label.getWidth());
		shipWindow.add(table);shipWindow.row();
		
		table = new Table();
		table.add(label = new Label("2", battleship.skin));
		table.add(Submarinebutton).minWidth(shipWindow.getWidth() - shipWindow.getPadLeft() - shipWindow.getPadRight() - label.getWidth());
		shipWindow.add(table); shipWindow.row();
		
		shipWindow.add(ConfirmButton = new TextButton("Confirm", battleship.skin));
		
		stage.addActor(shipWindow);
		shipWindow.setSize(shipWindow.getPrefWidth(), shipWindow.getPrefHeight());
		shipWindow.setPosition(Gdx.graphics.getWidth() - shipWindow.getWidth(), Gdx.graphics.getHeight() / 2 - shipWindow.getHeight() / 2);
		
		ConfirmButton.setVisible(false);
		ConfirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				shipWindow.remove();
				stageName.remove();
				
				Label label = new Label("Waiting for other player to position boats", battleship.skin);
				label.setSize(label.getPrefWidth(), label.getPrefHeight());
				label.setPosition(stage.getWidth() / 2.0f - label.getWidth() / 2.0f, stage.getHeight() / 2.0f - label.getHeight() / 2.0f);
				stage.addActor(label);
				
				MainClass.client.sendString("Battleship Boat Placed");
				isDone = true;
			}
		});
	}

	@Override
	public void update() {
		stage.act();
		
		if(isDone && isOtherDone)
			battleship.nextScene = new Battle(battleship);
	}

	@Override
	public void render() {
		battleship.renderScene(battleship.cam, battleship.modelBatch);
		battleship.renderWater();
		stage.draw();
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	public void event(String message) {
		switch(message){
		case "Battleship Boat Placed":
			Label label = new Label(battleship.manager.adversary.username + " has finished placing boats", battleship.skin);
			label.setSize(label.getPrefWidth(), label.getPrefHeight());
			label.setPosition(stage.getWidth() / 2.0f - label.getWidth() / 2.0f, 0);
			stage.addActor(label);
			isOtherDone = true;
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

	public boolean keyDown(int keycode) {
		if(shipWindow.hasParent())
			return stage.keyDown(keycode);
		return false;
	}

	public boolean keyUp(int keycode) {
		if(shipWindow.hasParent())
			return stage.keyUp(keycode);
		return false;
	}

	public boolean keyTyped(char character) {
		if(shipWindow.hasParent())
			return stage.keyTyped(character);
		return false;
	}

	public boolean touchDown(int x, int y, int pointer, int button) {
		if(shipWindow.hasParent())
			return stage.touchDown(x, y, pointer, button);
		return false;
	}

	public boolean touchUp(int x, int y, int pointer, int button) {
		if(shipWindow.hasParent() == false)
			return false;
		if(stage.touchUp(x, y, pointer, button))
			return true;
		return false;
	}

	public boolean touchDragged(int x, int y, int pointer) {
		if(shipWindow.hasParent())
			return stage.touchDragged(x, y, pointer);
		return false;
	}

	public boolean mouseMoved(int x, int y) {
		if(shipWindow.hasParent())
			return stage.mouseMoved(x, y);
		return false;
	}

	public boolean scrolled(int amount) {
		if(shipWindow.hasParent())
			return stage.scrolled(amount);
		return false;
	}
	
	public TextButton getShipButton(Ship.Type type) {
		switch(type) {
		case AircraftCarrier:
			return ACbutton;
		case Battleship:
			return Battleshipbutton;
		case Cruiser:
			return Cruiserbutton;
		case Destroyer:
			return Destroyerbutton;
		case Submarine:
			return Submarinebutton;
		default: // Should not ever reach this
			return null;
		}
	}
	
	public Ship.Type getNextType(Ship.Type type) { // Return next type in line, to go through ship button list
		if(type != Type.Submarine)
			return Ship.Type.values()[type.ordinal() + 1];
		else
			return Type.AircraftCarrier;
	}
	
	public boolean clicked(float x, float y) {
		if(battleship.assetsLoading == true)
			return true;
		if(stage.hit(x, y, true) != null)
			return true;
		Vector3 position = Battleship.RayWaterIntersection(battleship.cam.getPickRay(Gdx.input.getX(),Gdx.input.getY()));
		if(position.x < 0 || position.z < 0 || position.x > Battleship.CellWidth * 10 || position.z > Battleship.CellHeight * 10)
			return false;
		
		int xx = (int)Math.ceil(position.x / Battleship.CellWidth);
		int yy = (int)Math.ceil(position.z / Battleship.CellHeight);
		
		Ship ship = battleship.shipAt(xx, yy);
		
		if(PlaceButton.isChecked()) {
			if(ship != null) {
				ship.rotate();
			} else {
				Ship.Type type = Ship.Type.AircraftCarrier;
				if(ACbutton.isChecked()) {
					type = Ship.Type.AircraftCarrier;
				} else if(Battleshipbutton.isChecked()) {
					type = Ship.Type.Battleship;
				} else if(Cruiserbutton.isChecked()) {
					type = Ship.Type.Cruiser;
				} else if(Destroyerbutton.isChecked()) {
					type = Ship.Type.Destroyer;
				} else if(Submarinebutton.isChecked()) {
					type = Ship.Type.Submarine;
				}
				if(canPlaceShipType(type)) {
					new Ship(battleship, type, xx, yy);
					
					if(canPlaceShipType(type) == false) { // Disable button?
						TextButton button = getShipButton(type);
						button.setTouchable(Touchable.disabled);
						button.setColor(Color.GRAY);
						
						// Move selected button to next available button if there is one
						Ship.Type tmpType = getNextType(type);
						while(tmpType != type) {
							if(canPlaceShipType(tmpType)) { // Can still place boats of type, switch to this selected button
								shipGroup.setChecked(getShipButton(tmpType).getText().toString());
								break;
							}
							tmpType = getNextType(tmpType);
						}
						if(tmpType == type) { // All ships have been placed, no more button to jump too
							shipGroup.uncheckAll();
						}
					}
				}
				
				if(allShipsPlaced()) {
					ConfirmButton.setVisible(true);
				}
			}
		} else {
			if(ship != null) {
				ship.remove();
				
				// Re enable button
				TextButton button = getShipButton(ship.getType());
				button.setTouchable(Touchable.enabled);
				button.setColor(PlaceButton.getColor()); // Put back default background color
			}
			
			if(!allShipsPlaced()) {
				ConfirmButton.setVisible(false);
			}
		}
		
		return true;
	}
	
	private boolean allShipsPlaced() {
		return canPlaceShipType(Type.AircraftCarrier) == false && canPlaceShipType(Type.Battleship) == false && canPlaceShipType(Type.Cruiser) == false && canPlaceShipType(Type.Destroyer) == false && canPlaceShipType(Type.Submarine) == false;
	}
	
	// Calculate how much ships of type type is in the scene
	private int shipTypeCount(Type type) {
		int count = 0;
		for(int i = 0; i < battleship.ships.size(); i++)
			if(battleship.ships.get(i).getType() == type)
				count++;
		return count;
	}

	// Calculate if there is still space for a new ship of type type
	private boolean canPlaceShipType(Type type) {
		switch(type) {
		case Destroyer:
		case Submarine:
			if(shipTypeCount(type) < 2)
				return true;
			else
				return false;
		default:
			if(shipTypeCount(type) < 1)
				return true;
			else
				return false;
		}
	}
}
