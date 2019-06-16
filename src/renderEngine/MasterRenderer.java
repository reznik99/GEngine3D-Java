package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import shaders.FancyShader;
import shaders.StaticShader;
import shaders.TerrainShader;
import terrains.Terrain;

/**
 * Master renderer, contains all constants for all 
 * renderers and calls them.
 * @author Francesco
 */
public class MasterRenderer {
	
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;
	
	private Vector3f skyColor = new Vector3f(0, 0.7f, 0.7f);
	
	private Matrix4f projectionMatrix;
	
	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;
	
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();
	
	private FancyRenderer fancyRenderer;
	private FancyShader fancyShader = new FancyShader();
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
	private Map<TexturedModel, List<Entity>> fancyEntities = new HashMap<>();
	private List<Terrain> terrains = new ArrayList<>();
	
	
	public MasterRenderer() {
		//dont render polygons facing away from camera
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		fancyRenderer = new FancyRenderer(fancyShader, projectionMatrix);
	}
	
	public void render(Light sun, Camera camera) {
		
		prepare();
		
		//entities
		shader.start();
		shader.loadSkyColor(skyColor);
		shader.loadLight(sun);
		shader.loadViewMatrix(camera);
		renderer.render(entities);
		shader.stop();
		//terrain
		terrainShader.start();
		terrainShader.loadSkyColor(skyColor);
		terrainShader.loadLight(sun);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		//fancy stuff
		fancyShader.start();
		fancyShader.loadSkyColor(skyColor);
		fancyShader.loadLight(sun);
		fancyShader.loadViewMatrix(camera);
		fancyRenderer.render(fancyEntities);
		fancyShader.stop();

		
		terrains.clear();
		entities.clear();
		fancyEntities.clear();
	}
	
	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity) {
		TexturedModel texModel = entity.getModel();
		List<Entity> batch = entities.get(texModel);
		if(batch!=null) {//batch already exitst
			batch.add(entity);
		}
		else {
			List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			entities.put(texModel, newBatch);
		}
	}
	
	public void processFancyEntity(Entity entity) {
		TexturedModel texModel = entity.getModel();
		List<Entity> batch = fancyEntities.get(texModel);
		if(batch!=null) {//batch already exitst
			batch.add(entity);
		}
		else {
			List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			fancyEntities.put(texModel, newBatch);
		}
	}
	
	public void cleanUp() {
		shader.cleanUp();
		terrainShader.cleanUp();
		fancyShader.cleanUp();
	}  
	
	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);//zbuffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(skyColor.x, skyColor.y, skyColor.z, 1);
	}
	
    private void createProjectionMatrix(){
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;
 
        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
    }
    
}