package com.client.game.battleship;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.client.core.MainClass;
import com.client.core.OtherClient;
import com.client.core.Scene;
import com.client.core.SceneInput;
import com.client.game.Manager;

public class Battleship implements Scene, SceneInput, InputProcessor {
	
	public Skin skin;
	public SpriteBatch batch;
	
	public ClickListener clickListener;
	
	public Manager manager;
	public PerspectiveCamera cam;
	public CameraInputController camController;
	
	public Environment environment;
	public Cubemap cubemap;
	public Water water;
	
	public ModelBatch modelBatch;
	public Model model;
	
    public ArrayList<ModelInstance> instances = new ArrayList<>();
    public ArrayList<Ship> ships = new ArrayList<>(), otherShips = new ArrayList<>();
    public ParticleSystem particleSystem;
    
    public static final int CellWidth = 3, CellHeight = 3;
    public static final int WaterTop = 4;
    
    public AssetManager assetManager;
    public boolean assetsLoading = true;
    public Model AircraftCarrierModel, BattleshipModel, CruiserModel, DestroyerModel, SubmarineModel;
    public ParticleEffect waterEffect, fireEffect;
    
    public boolean turnToPlay = false;
    public boolean isWinner = false, isFinishedPlaying = false;
    
    private Scene currentScene; public Scene nextScene = null;
	public SceneInput currentSceneInput;
	protected DefaultShader mainShader;
	
	public Battleship(Manager manager) {
		this.manager = manager;
	}

	@Override
	public void init() {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		
		clickListener = new ClickListener() {
			@Override
			public boolean isOver(Actor actor, float x, float y) {
				return true;
			}
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Battleship.this.clicked(x, y);
			}
		};
		
		modelBatch = new ModelBatch(new DefaultShaderProvider() {
			
		});
		modelBatch.getRenderContext().setDepthTest(GL20.GL_LEQUAL);
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 10f, 0);
        cam.lookAt(0, 0, 0);//CellWidth * 5,0,CellHeight * 5);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0, -1f, 0));
        environment.add(new PointLight().setPosition(0, WaterTop + 1, 0).setColor(1, 1, 1, 1).setIntensity(1));
        
        cubemap = new Cubemap(new Pixmap(Gdx.files.internal("battleship/cubemap.png")));
        water = new Water();
        
        ModelBuilder modelBuilder = new ModelBuilder();
        ModelInstance instance;
        
        Model line = modelBuilder.createBox(10 * CellWidth, 0.2f, 0.2f, new Material(ColorAttribute.createDiffuse(Color.RED)),
                Usage.Position | Usage.Normal);
        
        for(int i = 0; i < 11; i++) {
        	instances.add(instance = new ModelInstance(line));
        	instance.transform.setTranslation(CellWidth * 5, WaterTop, i * CellHeight);
        }for(int i = 0; i < 11; i++) {
        	instances.add(instance = new ModelInstance(line));
        	instance.transform.setTranslation(i * CellWidth, WaterTop, CellHeight * 5);
        	instance.transform.rotate(0, 1, 0, 90);
        }
        
        model = modelBuilder.createBox(0.3f, 0.3f, 0.3f, 
                new Material(ColorAttribute.createDiffuse(Color.YELLOW)),
                Usage.Position | Usage.Normal);
        instances.add(instance = new ModelInstance(model));
        instance.transform.setTranslation(0, 0, 0);
        model = modelBuilder.createBox(0.3f, 0.3f, 0.3f, 
                new Material(ColorAttribute.createDiffuse(Color.RED)),
                Usage.Position | Usage.Normal);
        instances.add(instance = new ModelInstance(model));
        instance.transform.setTranslation(CellWidth * 10, 0, 0);
        model = modelBuilder.createBox(0.3f, 0.3f, 0.3f, 
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA)),
                Usage.Position | Usage.Normal);
        instances.add(instance = new ModelInstance(model));
        instance.transform.setTranslation(0, 0, CellHeight * 10);
        
        assetManager = new AssetManager();
        assetManager.load("battleship/AircraftCarrier.g3dj", Model.class);
        assetManager.load("battleship/Battleship.g3dj", Model.class);
        assetManager.load("battleship/Cruiser.g3dj", Model.class);
        assetManager.load("battleship/Destroyer.g3dj", Model.class);
        assetManager.load("battleship/Submarine.g3dj", Model.class);
        
        camController = new CameraInputController(cam);
        
        particleSystem = ParticleSystem.get();
        particleSystem.removeAll();
        particleSystem.getBatches().clear();
        BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
        billboardParticleBatch.setCamera(cam);
        particleSystem.add(billboardParticleBatch);
        
        ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
        assetManager.load("battleship/fire", ParticleEffect.class, loadParam);
        assetManager.load("battleship/waterSmoke", ParticleEffect.class, loadParam);

		BoatPlacing boatPlacing = new BoatPlacing(this);
        currentScene = boatPlacing;
        currentScene.init();
		currentSceneInput = boatPlacing;
        
        Gdx.input.setInputProcessor(this);
	}

	@Override
	public void update() {
		if(assetsLoading && assetManager.update()) {
			assetsLoading = false;
			AircraftCarrierModel = assetManager.get("battleship/AircraftCarrier.g3dj", Model.class);
			BattleshipModel = assetManager.get("battleship/Battleship.g3dj", Model.class);
			CruiserModel = assetManager.get("battleship/Cruiser.g3dj", Model.class);
			DestroyerModel = assetManager.get("battleship/Destroyer.g3dj", Model.class);
			SubmarineModel = assetManager.get("battleship/Submarine.g3dj", Model.class);
			
			waterEffect = assetManager.get("battleship/waterSmoke", ParticleEffect.class);
			fireEffect = assetManager.get("battleship/fire", ParticleEffect.class);
		}
		
		if(nextScene != null) {
			currentScene.dispose();
			currentScene = nextScene;
			currentScene.init();
			nextScene = null;
		}
		
		camController.update();
		currentScene.update();
		particleSystem.update();
	}

	@Override
	public void render() {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        renderCubemap(cam);
        currentScene.render();
	}
	
	public void renderCubemap(PerspectiveCamera camera) {
		cubemap.render(camera);
	}
	
	public void renderScene(PerspectiveCamera camera, ModelBatch modelBatch) {
		modelBatch.begin(camera);
        for(ModelInstance instance : instances) {
        	modelBatch.render(instance, environment);
        }
        for(Ship ship : turnToPlay ? otherShips : ships) {
        	if(turnToPlay && ship.isDestroyed() == false) // Don't render enemy ships that are not sunk
        		continue;
        	ship.draw(modelBatch);
        }
        
        particleSystem.begin();
        particleSystem.draw();
        particleSystem.end();
        modelBatch.render(particleSystem);
        
        modelBatch.end();
	}
	
	public void renderWater() {
		water.render(cam);
	}
	
	public static Vector3 RayWaterIntersection(Ray ray) {
		float k = (Battleship.WaterTop - ray.origin.y) / ray.direction.y;
		return new Vector3(ray.origin.x + k * ray.direction.x, Battleship.WaterTop, ray.origin.z + k * ray.direction.z);
	}

	@Override
	public void dispose() {
		currentScene.dispose();
		
        modelBatch.dispose();
		model.dispose();
		cubemap.dispose();
		water.dispose();
		particleSystem.removeAll();
		fireEffect.dispose();
		waterEffect.dispose();
		
		assetManager.dispose();
	}

	@Override
	public void event(String message) {
		currentScene.event(message);
		
		switch(message) {
		case "Battleship Boat Description":
			new Ship(this, Ship.Type.values()[MainClass.client.readInt()], MainClass.client.readInt(), MainClass.client.readInt(), MainClass.client.readInt());
			break;
		case "Battleship turn to play":
			turnToPlay = MainClass.client.readBool();
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

	@Override
	public boolean keyDown(int keycode) {
		if(currentSceneInput.keyDown(keycode))
			return true;
		return camController.keyDown(keycode);
	}

	@Override
	public boolean keyUp(int keycode) {
		if(currentSceneInput.keyUp(keycode))
			return true;
		return camController.keyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		if(currentSceneInput.keyTyped(character))
			return true;
		return camController.keyTyped(character);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		clickListener.touchDown(null, x, y, pointer, button);
		if(currentSceneInput.touchDown(x, y, pointer, button))
			return true;
		return camController.touchDown(x, y, pointer, button);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		InputEvent event = new InputEvent(); event.setListenerActor(null);
		clickListener.touchUp(event, x, y, pointer, button);
		if(currentSceneInput.touchUp(x, y, pointer, button))
			return true;
		return camController.touchUp(x, y, pointer, button);
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if(Gdx.input.getDeltaX() > 2 || Gdx.input.getDeltaY() > 2)
			clickListener.cancel();
		if(currentSceneInput.touchDragged(x, y, pointer))
			return true;
		return camController.touchDragged(x, y, pointer);
	}

	@Override
	public boolean mouseMoved(int x, int y) {
		if(currentSceneInput.mouseMoved(x, y))
			return true;
		return camController.mouseMoved(x, y);
	}

	@Override
	public boolean scrolled(int amount) {
		if(currentSceneInput.scrolled(amount))
			return true;
		return camController.scrolled(amount);
	}

	public boolean clicked(float x, float y) {
		return currentSceneInput.clicked(x, y);
	}
	
	/* Ship at grid coordinates */
	public Ship shipAt(int x, int y) {
		for(int i = 0; i < ships.size(); i++) {
			if(ships.get(i).occupySpace(x, y))
				return ships.get(i);
		}
		return null;
	}
	
	public Ship shipAt(int x, int y, ArrayList<Ship> ships) {
		for(int i = 0; i < ships.size(); i++) {
			if(ships.get(i).occupySpace(x, y))
				return ships.get(i);
		}
		return null;
	}
}
