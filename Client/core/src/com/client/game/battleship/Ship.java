package com.client.game.battleship;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class Ship {
	private Battleship battleship;
	private ModelInstance instance;
	
	enum Type {
		AircraftCarrier,
		Battleship,
		Cruiser,
		Destroyer,
		Submarine
	}
	
	private Type type;
	public int x, y;
	public int rotation;
	public boolean tileHit[];
	
	public Ship(Battleship battleship, Type type, int x, int y) {
		this.battleship = battleship;
		this.type = type;
		
		this.x = x;
		this.y = y;
		this.rotation = 0;
		
		tileHit = new boolean[length()];
		for(int i = 0; i < tileHit.length; i++)
			tileHit[i] = false;
		
		while(!canPlace()) {
			if(rotation == 270)
				return;
			rotation += 90;
		}
		
		battleship.ships.add(this);
		
		switch(type) {
		case AircraftCarrier:
			instance = new ModelInstance(battleship.AircraftCarrierModel);
			break;
		case Battleship:
			instance = new ModelInstance(battleship.BattleshipModel);
			break;
		case Cruiser:
			instance = new ModelInstance(battleship.CruiserModel);
			break;
		case Destroyer:
			instance = new ModelInstance(battleship.DestroyerModel);
			break;
		case Submarine:
			instance = new ModelInstance(battleship.SubmarineModel);
			break;
		}
		
		this.calculatePosition();
	}
	
	public Ship(Battleship battleship, Type type, int x, int y, int rotation) { // Constructor for adversaries boats
		this.battleship = battleship;
		this.type = type;
		
		this.x = x;
		this.y = y;
		this.rotation = rotation;
		
		battleship.otherShips.add(this);
		
		tileHit = new boolean[length()];
		for(int i = 0; i < tileHit.length; i++)
			tileHit[i] = false;
		
		switch(type) {
		case AircraftCarrier:
			instance = new ModelInstance(battleship.AircraftCarrierModel);
			break;
		case Battleship:
			instance = new ModelInstance(battleship.BattleshipModel);
			break;
		case Cruiser:
			instance = new ModelInstance(battleship.CruiserModel);
			break;
		case Destroyer:
			instance = new ModelInstance(battleship.DestroyerModel);
			break;
		case Submarine:
			instance = new ModelInstance(battleship.SubmarineModel);
			break;
		}
		
		this.calculatePosition();
	}

	boolean canPlace() {
		int length = length() - 1;
		
		if(rotation == 0 && x + length > 10)
			return false;
		if(rotation == 90 && y + length > 10)
			return false;
		if(rotation == 180 && x - length <= 0)
			return false;
		if(rotation == 270 && y - length <= 0)
			return false;
		
		for(int i = 0; i < battleship.ships.size(); i++) {
			if(battleship.ships.get(i) == this)
				continue;
			
			Ship ship = battleship.ships.get(i);
			
			for(int j = 0; j < length(); j++) {
				if(rotation == 0) {
					if(ship.occupySpace(x + j, y))
						return false;
				} else if(rotation == 90) {
					if(ship.occupySpace(x, y + j))
						return false;
				} else if(rotation == 180) {
					if(ship.occupySpace(x - j, y))
						return false;
				} else if(rotation == 270) {
					if(ship.occupySpace(x, y - j))
						return false;
				}
			}
		}
		return true;
	}
	
	void hit(int x, int y) {
		if(rotation == 0 || rotation == 180) {
			tileHit[Math.abs(this.x - x)] = true;
		} else {
			tileHit[Math.abs(this.y - y)] = true;
		}
	}
	
	boolean occupySpace(int x, int y) {
		if(x == this.x && y == this.y)
			return true;
		
		if(x == this.x && ((rotation == 90)|(rotation == 270))) {
			if(rotation == 90 && y > this.y && y < this.y + length())
				return true;
			if(rotation == 270 && y < this.y && y > this.y - length())
				return true;
		}
		if(y == this.y && ((rotation == 0)|(rotation == 180))) {
			if(rotation == 0 && x > this.x && x < this.x + length())
				return true;
			if(rotation == 180 && x < this.x && x > this.x - length())
				return true;
		}
		
		return false;
	}
	
	public void rotate() {
		rotation += 90;
		while(!canPlace()) {
			rotation += 90;
			
			if(rotation == 360)
				rotation = 0;
		}
		while(rotation >= 360)
			rotation -= 360;
		this.calculatePosition();
	}
	
	public void calculatePosition() {
		float xx = x - 1;
		float yy = y - 1;
		
		if(rotation == 0) {
			xx += length() / 2.0f;
			yy += 0.5f;
		}
		else if(rotation == 90) {
			yy += length() / 2.0f;
			xx += 0.5f;
		}
		else if(rotation == 180) {
			xx -= length() / 2.0f - 1;
			yy += 0.5f;
		}
		else if(rotation == 270) {
			xx += 0.5f;
			yy -= length() / 2.0f - 1;
		}
		
		float originx = xx * Battleship.CellWidth;
		float originz = yy * Battleship.CellHeight;
		instance.transform.idt();
		instance.transform.rotate(0, 1, 0, 360 - rotation);
		instance.transform.setTranslation(originx, Battleship.WaterTop, originz);
	}
	
	public void draw(ModelBatch modelBatch) {
		modelBatch.render(instance, battleship.environment);
	}
	
	public void remove() {
		battleship.ships.remove(this);
	}
	
	public boolean isDestroyed() {
		for(boolean tileHit : this.tileHit)
			if(tileHit == false)
				return false;
		return true;
	}
	
	public int length() {
		switch(type) {
		case AircraftCarrier:
			return 5;
		case Battleship:
			return 4;
		case Cruiser:
			return 3;
		case Destroyer:
			return 2;
		case Submarine:
			return 1;
		default:
			return 1;
		}
	}
	
	public Type getType() {
		return type;
	}
}
