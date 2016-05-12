package com.client.game.pendu;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.core.Timer;
import com.client.core.TimerCallback;
import com.client.game.Manager;

public class Pendu implements Scene, TimerCallback {
	private Stage stage;
	private Skin skin;
	private SpriteBatch batch;
	
	public Manager manager;
	
	public Table table;
	
	final int POINT_POUR_GAGNER = 13;
	
	public String motADeviner = null;
	public StringBuilder motDeviner = null;
	public int vie = 0;
	
	public Label scoreAffiche;
	public Label motAffiche, vieAffiche;
	public TextButton confirmeLettre;
	public TextField entreLettre;
	public Label statusAutre;
	
	public boolean autreJoueurFini = false, autreJoueurReussi;
	public boolean joueurFini = false, joueurFiniEnPremier, joueurReussi;
	public boolean partieEnCour = true; // La partie se fini quand le premier atteint 13 ou plus points
	
	private int pointJoueur = 0, pointAdversaire = 0;
	
	public Pendu(Manager manager) {
		this.manager = manager;
	}
	
	@Override
	public void init() {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		
		stage = new Stage();
		
		if(manager.isServer()) {
			commenceParti();
		}
		
		table = new Table(skin);
		table.setBounds(0, 0, stage.getWidth(), stage.getHeight());
		
		stage.addActor(table);
		
		scoreAffiche = new Label(MainClass.client.getUsername() + " : " + String.valueOf(pointJoueur) + " | " + manager.adversary.username + " : " + String.valueOf(pointAdversaire), skin);
		table.add(scoreAffiche);
		table.row();
		
		Gdx.input.setInputProcessor(stage);
	}
	
	public void commenceParti() {
		motADeviner = choisitMotAuHasard();

		MainClass.client.sendString("Pendu Mot Choisit");
		MainClass.client.sendString(motADeviner);
	}
	
	public String choisitMotAuHasard() {
		String mot = null;
		ArrayList<String> mots = new ArrayList<>();

		try {
			BufferedReader br = new BufferedReader(Gdx.files.internal("pendu/liste_mots.txt").reader());
			while (br.ready()) {
				mots.add(br.readLine());
			}
			br.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		final Random rand = new Random();
		mot = mots.get(rand.nextInt(mots.size()));
		System.out.println("Mot a deviner Server : " + mot);
		return mot;
	}

	@Override
	public void update() {
		if(motADeviner != null && table.getChildren().size == 1 && partieEnCour) { // Le mot et choisi et l'interface a encore rien a part le score, faire l'interface graphique
			 motDeviner = new StringBuilder(motADeviner); 
			for(int i = 0; i < motADeviner.length(); i++)
				motDeviner.setCharAt(i, '_');
			
			vie = 10;
			
			motAffiche = new Label("Mot : " + motDeviner.toString(), skin);
			table.add(motAffiche);table.row();
			
			vieAffiche = new Label("Vie restante : "+ String.valueOf(vie) ,skin);
			table.add(vieAffiche);table.row();

			entreLettre = new TextField("", skin);
			entreLettre.setMaxLength(1);
			table.add(entreLettre);
			
			confirmeLettre = new TextButton("Confirme lettre", skin);
			table.add(confirmeLettre);table.row();

			confirmeLettre.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(entreLettre.getText().length() == 0)
						return;
					char lettre = entreLettre.getText().charAt(0);
					
					if (motADeviner.indexOf(lettre) == -1) {
						vie--;

						if (vie == 0) {
							roundFinished(false);
						}
					} else {
						int index = motADeviner.indexOf(lettre);
						while (index >= 0) { 
							motDeviner.setCharAt(index, lettre);
							index = motADeviner.indexOf(lettre, index + 1);
						}
						
						if (motADeviner.equals(motDeviner.toString())) {
							roundFinished(true);
						}
					}
					
					entreLettre.setText("");
					motAffiche.setText("Mot : " + motDeviner.toString());
					vieAffiche.setText("Vie restante : "+ String.valueOf(vie));
				}
			});
			
			statusAutre = new Label("Other player is guessing", skin);
			table.add(statusAutre);
			
			stage.addActor(table);
		}

		if(manager.isServer() && joueurFini && autreJoueurFini && partieEnCour) { // Fin de parti, nouvelle parti
			if(autreJoueurReussi == false && joueurReussi == true) {
				pointJoueur += 3;
				if(joueurFiniEnPremier == true) // Autre joueurs perd et fini dernier
					pointAdversaire -= 1;
			} else if(autreJoueurReussi == true && joueurReussi == false) {
				pointAdversaire += 3;
				if(joueurFiniEnPremier == false)
					pointJoueur -= 1;
			} else if(autreJoueurReussi == true && joueurReussi == true) { // Si le joueur et l'adversaire reussissent
				if(joueurFiniEnPremier) {
					pointJoueur += 3;
					pointAdversaire += 1;
				} else {
					pointAdversaire += 3;
					pointJoueur += 1;
				}
			}
			
			MainClass.client.sendString("Pendu Points");
			MainClass.client.sendInt(pointAdversaire);
			MainClass.client.sendInt(pointJoueur);
			
			joueurFini = false; autreJoueurFini = false;
			
			if(pointJoueur >= POINT_POUR_GAGNER && pointAdversaire >= POINT_POUR_GAGNER) { // Tous les deux gagne
				table.add(new Label("Draw", skin));
				partieEnCour = false;
				new Timer(4000).setCallback(this);
			} else if(pointJoueur >= POINT_POUR_GAGNER) { // Gagner
				table.add(new Label("You won", skin));
				partieEnCour = false;
				new Timer(4000).setCallback(this);
			} else if(pointAdversaire >= POINT_POUR_GAGNER) { // Perdu
				table.add(new Label("You lost", skin));
				partieEnCour = false;
				new Timer(4000).setCallback(this);
			} else
				commenceParti();
		}
		
		scoreAffiche.setText(MainClass.client.getUsername() + " : " + String.valueOf(pointJoueur) + " | " + manager.adversary.username + " : " + String.valueOf(pointAdversaire));
	}

	private void roundFinished(boolean succeeded) {
		MainClass.client.sendString("Pendu Joueur Fini");
		MainClass.client.sendBool(succeeded);
		
		motADeviner = null;
		
		table.clear();
		table.add(scoreAffiche);
		table.row();
		
		joueurFini = true;
		if(autreJoueurFini == false)
			joueurFiniEnPremier = true;
		else
			joueurFiniEnPremier = false;
		
		joueurReussi = succeeded;
	}
	
	@Override
	public void timerCallback(String name) {
		if(pointJoueur >= POINT_POUR_GAGNER && pointAdversaire >= POINT_POUR_GAGNER) { // Tous les deux gagne
			manager.gameEnded(true, true);
		} else if(pointJoueur >= POINT_POUR_GAGNER) { // Gagner
			manager.gameEnded(true, false);
		} else if(pointAdversaire >= POINT_POUR_GAGNER) { // Perdu
			manager.gameEnded(false, true);
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
		switch(message) {
		case "Pendu Joueur Fini":
			autreJoueurFini = true;
			autreJoueurReussi = MainClass.client.readBool();
			
			if(autreJoueurReussi) {
				statusAutre.setText(manager.adversary.username + " is done");
			} else {
				statusAutre.setText(manager.adversary.username + " has failed");
			}
			break;
		case "Pendu Points":
			pointJoueur = MainClass.client.readInt();
			pointAdversaire = MainClass.client.readInt();
			
			if(pointJoueur >= POINT_POUR_GAGNER && pointAdversaire >= POINT_POUR_GAGNER) { // Tous les deux gagne
				table.add(new Label("Draw", skin));
				partieEnCour = false;
				new Timer(4000).setCallback(this);
			} else if(pointJoueur >= POINT_POUR_GAGNER) { // Gagner
				table.add(new Label("You won", skin));
				partieEnCour = false;
				new Timer(4000).setCallback(this);
			} else if(pointAdversaire >= POINT_POUR_GAGNER) { // Perdu
				table.add(new Label("You lost", skin));
				partieEnCour = false;
				new Timer(4000).setCallback(this);
			}
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
