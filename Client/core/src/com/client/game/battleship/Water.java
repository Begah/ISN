package com.client.game.battleship;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Water {
	protected static ShaderProgram createMeshShader() {
		ShaderProgram.pedantic = false;
		
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal("battleship/shaders/waterVertex.txt"), Gdx.files.internal("battleship/shaders/waterFragment.txt"));
		String log = shader.getLog();
		if (!shader.isCompiled())
			throw new GdxRuntimeException(log);		
		if (log!=null && log.length()!=0)
			System.out.println("Shader Log: "+log);
		return shader;
	}

	// 8 pour tablette axelle
	public static final float TILE_WIDTH = 2f;
	public static final float TILE_DEPTH = 2f;
	
	public float redstartx, redstartz, redendx, redendz;
	
	Texture texture;
	Mesh mesh;
	ShaderProgram shader;
	
	public Water() {
		mesh = createQuad(800, 800);
		
		shader = createMeshShader();
		texture = new Texture(Gdx.files.internal("battleship/water.jpg"));
		
		redstartx = redstartz = 0;
		redendx = redendz = 0;
	}
	
	float f = 0;
	
	public void render(PerspectiveCamera camera) {
		f += 0.01;
		shader.begin();
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		shader.setUniformMatrix("mvp", camera.combined);
		shader.setUniformf("time", f);
		shader.setUniformf("TILE_WIDTH", TILE_WIDTH);
		shader.setUniformf("TILE_DEPTH", TILE_DEPTH);
		shader.setUniformf("redstart", new Vector2(redstartx, redstartz));
		shader.setUniformf("redend", new Vector2(redendx, redendz));
		
		shader.setUniformf("texture", 0);
		texture.bind();

		mesh.render(shader, GL20.GL_TRIANGLES);
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		shader.end();
		
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}
	
	public void dispose() {
		mesh.dispose();
		shader.dispose();
	}
	
	public Mesh createQuad(float width, float height) {
		Mesh mesh = new Mesh(true, (int)((width / TILE_WIDTH) * (height / TILE_DEPTH)) * 2 * 3, 0, new VertexAttribute(Usage.Position, 4, "position"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_uv"));
		float x1 = -width / 2.0f, z1 = -height / 2.0f;
		float textureTileWidth = 1.0f / (width / TILE_WIDTH / 16.0f), textureTileHeight = 1.0f / (height / TILE_DEPTH / 16.0f);
		float[] data = new float[(int)((width / TILE_WIDTH) * (height / TILE_DEPTH)) * 2 * 3 * 6];
		
		int i = 0;
		for(int x = 0; x < width / TILE_WIDTH; x++) {
			for(int z = 0; z < height / TILE_DEPTH; z++) {
				float textx1 = (x * textureTileWidth) % 1, textz1 = (z * textureTileHeight) % 1;
				float textx2 = ((x + 1) * textureTileWidth) % 1, textz2 = ((z + 1) * textureTileHeight) % 1;
				
				if(textx2 < textx1) { // In a tile, can't have it drawing two different textures
					textx2 = 1;
				}
				if(textz2 < textz1) {
					textz2 = 1;
				}
				
				data[i] = x1 + x * TILE_WIDTH;
				data[i + 1] = Battleship.WaterTop;
				data[i + 2] = z1 + z * TILE_DEPTH;
				data[i + 3] = 1;
				data[i + 4] = textx1;
				data[i + 5] = textz1;
				
				data[i + 6] = x1 + (x + 1) * TILE_WIDTH;
				data[i + 7] = Battleship.WaterTop;
				data[i + 8] = z1 + z * TILE_DEPTH;
				data[i + 9] = 2;
				data[i + 10] = textx2;
				data[i + 11] = textz1;
				
				data[i + 12] = x1 + x * TILE_WIDTH;
				data[i + 13] = Battleship.WaterTop;
				data[i + 14] = z1 + (z + 1) * TILE_DEPTH;
				data[i + 15] = 4;
				data[i + 16] = textx1;
				data[i + 17] = textz2;
				
				data[i + 18] = x1 + (x + 1) * TILE_WIDTH;
				data[i + 19] = Battleship.WaterTop;
				data[i + 20] = z1 + z * TILE_DEPTH;
				data[i + 21] = 5;
				data[i + 22] = textx2;
				data[i + 23] = textz1;
				
				data[i + 24] = x1 + (x + 1) * TILE_WIDTH;
				data[i + 25] = Battleship.WaterTop;
				data[i + 26] = z1 + (z + 1) * TILE_DEPTH;
				data[i + 27] = 3;
				data[i + 28] = textx2;
				data[i + 29] = textz2;
				
				data[i + 30] = x1 + x * TILE_WIDTH;
				data[i + 31] = Battleship.WaterTop;
				data[i + 32] = z1 + (z + 1) * TILE_DEPTH;
				data[i + 33] = 6;
				data[i + 34] = textx1;
				data[i + 35] = textz2;
				
				i += 36;
			}
		}
		
		mesh.setVertices(data);
		return mesh;
	}
}
