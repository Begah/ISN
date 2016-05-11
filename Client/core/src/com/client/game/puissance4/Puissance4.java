package com.client.game.puissance4;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.core.Timer;
import com.client.core.TimerCallback;
import com.client.game.Manager;

public class Puissance4 implements Scene, TimerCallback {
	int [][] cases = new int[7][6];                //definition cases tableau
	Texture imageCase, imageJeton1, imageJeton2;   //definition des images
	
	final int JetonVitesse = 4;
	Vector2 jetonDescendant = new Vector2(-1, -1), jetonAajouter = new Vector2();
	float stopAtY; int jetonAjouterJoueur;
	
	Stage stage;
	Skin skin;                                      //type de texte (autre fichier)
	Label labelTourDeJoueur;
	
	SpriteBatch spriteBatch = new SpriteBatch();    //nouvelle feuille blanche
	Manager manager;
	ClickListener listener;
	
	boolean jouerTourDeJouer = false;                //boolean = variable true or false
	boolean entrainDeJouer = true;
	int gagnant = 0;
	
	public Puissance4(Manager manager) {
		this.manager = manager;
	}

	@Override
	public void init() {                            //creation du tableau
		for(int x = 0; x < 7; x++) {                //7 lignes sur x
			for(int y = 0; y < 6; y++) {            //6 lignes sur y
				cases[x][y] = 0;                    //commence a [0][0]
			}
		}
		imageCase = new Texture(Gdx.files.internal("puissance4/Case.png"));      //nom image + lieu
		imageJeton1 = new Texture(Gdx.files.internal("puissance4/Jeton 1.png"));
		imageJeton2 = new Texture(Gdx.files.internal("puissance4/Jeton 2.png"));
		
		stage = new Stage();
		skin = new Skin(Gdx.files.internal("skin/uiskin.json")); //pour presentation textes
		labelTourDeJoueur = new Label("C'est a l'autre de jouer", skin);
		stage.addActor(labelTourDeJoueur);
		
		stage.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(jouerTourDeJouer == true && entrainDeJouer == true) {
					float caseWidth = Gdx.graphics.getWidth() / 7.0f;
					
					int xx = (int)(x / caseWidth); //definition place du jeton
					
					for(int yy = 0; yy < 6; yy++) {
						if(cases[xx][yy] == 0) {						
							MainClass.client.sendString("Puissance4 place jeton");
							MainClass.client.sendInt(xx);
							MainClass.client.sendInt(yy);
							
							jetonDescendant.set(xx * (Gdx.graphics.getWidth() / 7.0f), Gdx.graphics.getHeight());
							jetonAajouter.set(xx, yy);
							stopAtY = yy * (Gdx.graphics.getHeight() / 6.25f);
							if(manager.isServer()) {
								jetonAjouterJoueur = 1;
							} else {
								jetonAjouterJoueur = 2;
							}
							break;
						}
					}
				}
			}
		});
		Gdx.input.setInputProcessor(stage);
		
		if(manager.isServer()) {			
			jouerTourDeJouer = true;
		}
	}
	
	void checkWinner() {
		int winner = 0;
		
		Quit:
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 3; y++) {
				if(cases[x][y] == 0)
					continue;
				if(cases[x][y] == cases[x + 1][y] && cases[x][y] == cases[x + 2][y] && cases[x][y] == cases[x + 3][y]) { //alignement 4 jetons sur x
					winner = cases[x][y];
					break Quit;
				}
				if(cases[x][y] == cases[x][y + 1] && cases[x][y] == cases[x][y + 2] && cases[x][y] == cases[x][y + 3]) { //alignement 4 jetons sur y
					winner = cases[x][y];
					break Quit; //break = 2�me pas soumis � premier
				}
				if(cases[x][y] == cases[x + 1][y + 1] && cases[x][y] == cases[x + 2][y + 2] && cases[x][y] == cases[x + 3][y + 3]) { // alignement 4 jetons sur x et y
					winner = cases[x][y];
					break Quit;
				}
			}
		}
		
		if(winner != 0) {
			MainClass.client.sendString("Puissance4 Gagnant");
			MainClass.client.sendInt(winner);
			
			entrainDeJouer = false;
			gagnant = winner;
			if(winner == 1)
				labelTourDeJoueur.setText("You won");
			else if(winner == 2)
				labelTourDeJoueur.setText("You lost");
			
			new Timer(4000).setCallback(this);
		}
	}

	@Override
	public void update() {
		stage.act();
		
		if(manager.isServer() && entrainDeJouer)
			checkWinner();
		
		if(jouerTourDeJouer == true && entrainDeJouer == true)
			labelTourDeJoueur.setText("A ton tour de jouer");
		else if(entrainDeJouer == true)
			labelTourDeJoueur.setText("A " + manager.adversary.username + " de jouer");
		labelTourDeJoueur.setSize(labelTourDeJoueur.getPrefWidth(), labelTourDeJoueur.getPrefHeight());
		labelTourDeJoueur.setPosition(Gdx.graphics.getWidth() / 2.0f - labelTourDeJoueur.getWidth() / 2.0f, Gdx.graphics.getHeight() - labelTourDeJoueur.getHeight());
	}

	@Override
	public void render() {
		spriteBatch.begin();
		
		float caseWidth = Gdx.graphics.getWidth() / 7.0f;
		float caseHeight = Gdx.graphics.getHeight() / 6.25f;
		
		if(jetonDescendant.epsilonEquals(-1, -1, 0) == false) {
			spriteBatch.draw(jetonAjouterJoueur == 1 ? imageJeton1 : imageJeton2, jetonDescendant.x, jetonDescendant.y, caseWidth, caseHeight);
			jetonDescendant.y -= JetonVitesse;
			
			if(jetonDescendant.y <= stopAtY) {
				cases[(int)jetonAajouter.x][(int)jetonAajouter.y] = jetonAjouterJoueur;
				jetonDescendant.set(-1, -1);
				
				jouerTourDeJouer = !jouerTourDeJouer;
			}
		}
		
		for(int x = 0; x < 7; x++) {
			for(int y = 0; y < 6; y++) {
				spriteBatch.draw(imageCase, x * caseWidth, y * caseHeight, caseWidth, caseHeight);
				
				if(cases[x][y] == 1)
					spriteBatch.draw(imageJeton1, x * caseWidth, y * caseHeight, caseWidth, caseHeight);
				else if(cases[x][y] == 2)
					spriteBatch.draw(imageJeton2, x * caseWidth, y * caseHeight, caseWidth, caseHeight);
			}
		}
		
		spriteBatch.end();
		
		stage.draw();
	}
	
	@Override
	public void timerCallback(String name) {
		// Partie fini, renvoyer le controle a l'application
		
		boolean aGagner = false;
		if(manager.isServer() && gagnant == 1)
			aGagner = true;
		if(!manager.isServer() && gagnant == 2)
			aGagner = true;

		manager.gameEnded(aGagner, !aGagner);
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void event(String message) {
		switch(message) {
		case "Puissance4 place jeton":
			int x = MainClass.client.readInt(); int y = MainClass.client.readInt();
			jetonDescendant.set(x * (Gdx.graphics.getWidth() / 7.0f), Gdx.graphics.getHeight());
			jetonAajouter.set(x, y);
			stopAtY = y * (Gdx.graphics.getHeight() / 6.25f);
			if(manager.isServer()) {
				jetonAjouterJoueur = 2;
			} else {
				jetonAjouterJoueur = 1;
			}
			break;
		case "Puissance4 Gagnant":
			entrainDeJouer = false;
			gagnant = MainClass.client.readInt();
			if(gagnant == 2)
				labelTourDeJoueur.setText("You won");
			else if(gagnant == 1)
				labelTourDeJoueur.setText("You lost");
			
			new Timer(4000).setCallback(this);
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
