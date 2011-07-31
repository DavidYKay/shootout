package com.davidykay.shootout;

/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.io.InputStream;
import java.util.ArrayList;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderOld;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.davidykay.shootout.simulation.Block;
import com.davidykay.shootout.simulation.Explosion;
import com.davidykay.shootout.simulation.Invader;
import com.davidykay.shootout.simulation.RayShot;
import com.davidykay.shootout.simulation.Ship;
import com.davidykay.shootout.simulation.Shot;
import com.davidykay.shootout.simulation.Simulation;

/**
 * The renderer receives a simulation and renders it.
 * @author mzechner
 *
 */
public class Renderer {
  private static final String TAG = "Renderer";
  /** sprite batch to draw text **/
  private SpriteBatch spriteBatch;
  /** the moon mesh **/
  private Mesh moonMesh;
  /** the moon texture **/
  private Texture moonTexture;
  /** the ship mesh **/
  private Mesh shipMesh;
  /** the ship texture **/
  private Texture shipTexture;
  /** the invader mesh **/
  private Mesh invaderMesh;
  /** the invader texture **/
  private Texture invaderTexture;
  /** the block mesh **/
  private Mesh blockMesh;
  /** the shot mesh **/
  private Mesh shotMesh;
  /** the ray mesh **/
  private Mesh rayMesh;
  /** the background texture **/
  private Texture backgroundTexture;
  /** the earth texture **/
  private Texture earthTexture;
  /** the explosion mesh **/
  private Mesh explosionMesh;
  /** the explosion texture **/
  private Texture explosionTexture;
  /** the font **/
  private BitmapFont font;
  /** the rotation angle of all invaders around y **/
  private float invaderAngle = 0;
  /** status string **/
  private String status = "";
  /** keeping track of the last score so we don't constantly construct a new string **/
  private int lastScore = 0;
  private int lastLives = 0;
  private int lastWave = 0;

  /** view and transform matrix for text rendering **/
  private Matrix4 viewMatrix = new Matrix4();
  private Matrix4 transformMatrix = new Matrix4();

  /** perspective camera **/
  private PerspectiveCamera camera;

  public Renderer (Application app) {
    try {
      spriteBatch = new SpriteBatch();

      //InputStream in = Gdx.files.internal("data/ship.obj").read();
      //InputStream in = Gdx.files.internal("data/cube_01.obj").read();
      InputStream in = Gdx.files.internal("data/emplacement.obj").read();
      shipMesh = ModelLoaderOld.loadObj(in);
      in.close();

      in = Gdx.files.internal("data/moon.obj").read();
      moonMesh = ModelLoaderOld.loadObj(in);
      in.close();

      //in = Gdx.files.internal("data/invader.obj").read();
      in = Gdx.files.internal("data/ufo.obj").read();
      invaderMesh = ModelLoaderOld.loadObj(in);
      in.close();

      in = Gdx.files.internal("data/block.obj").read();
      blockMesh = ModelLoaderOld.loadObj(in);
      in.close();

      in = Gdx.files.internal("data/shot.obj").read();
      shotMesh = ModelLoaderOld.loadObj(in);
      in.close();

      rayMesh = shotMesh;

      moonTexture = new Texture(Gdx.files.internal("data/moon.png"), Format.RGB565, true);
      moonTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
      shipTexture = new Texture(Gdx.files.internal("data/battery.png"), Format.RGB565, true);
      shipTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);

      //invaderTexture = new Texture(Gdx.files.internal("data/invader.png"), Format.RGB565, true);
      invaderTexture = new Texture(Gdx.files.internal("data/ufo.png"), Format.RGB565, true);
      //invaderTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
      //invaderTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

      backgroundTexture = new Texture(Gdx.files.internal("data/starfield512.png"), Format.RGB565, true);
      backgroundTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
      earthTexture = new Texture(Gdx.files.internal("data/marble128.jpg"), Format.RGB565, true);
      earthTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
      explosionTexture = new Texture(Gdx.files.internal("data/explode.png"), Format.RGBA4444, true);
      explosionTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);

      explosionMesh = new Mesh(true, 4 * 16, 0, new VertexAttribute(Usage.Position, 3, "a_position"),
        new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord"));

      float[] vertices = new float[4 * 16 * (3 + 2)];
      int idx = 0;
      for (int row = 0; row < 4; row++) {
        for (int column = 0; column < 4; column++) {
          vertices[idx++] = 1;
          vertices[idx++] = 1;
          vertices[idx++] = 0;
          vertices[idx++] = 0.25f + column * 0.25f;
          vertices[idx++] = 0 + row * 0.25f;

          vertices[idx++] = -1;
          vertices[idx++] = 1;
          vertices[idx++] = 0;
          vertices[idx++] = 0 + column * 0.25f;
          vertices[idx++] = 0 + row * 0.25f;

          vertices[idx++] = -1;
          vertices[idx++] = -1;
          vertices[idx++] = 0;
          vertices[idx++] = 0f + column * 0.25f;
          vertices[idx++] = 0.25f + row * 0.25f;

          vertices[idx++] = 1;
          vertices[idx++] = -1;
          vertices[idx++] = 0;
          vertices[idx++] = 0.25f + column * 0.25f;
          vertices[idx++] = 0.25f + row * 0.25f;
        }
      }

      explosionMesh.setVertices(vertices);
      font = new BitmapFont(Gdx.files.internal("data/font10.fnt"), Gdx.files.internal("data/font10.png"), false);

      camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void render (Application app, Simulation simulation) {
    GL10 gl = app.getGraphics().getGL10();
    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    gl.glViewport(0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight());

    renderBackground(gl);
    renderEarth(gl);

    gl.glDisable(GL10.GL_DITHER);
    gl.glEnable(GL10.GL_DEPTH_TEST);
    gl.glEnable(GL10.GL_CULL_FACE);

    //setProjectionAndCamera(app.getGraphics(), simulation.ship, app);
    setProjectionAndCameraAugmentedReality(app.getGraphics(), simulation, app);

    setLighting(gl);

    gl.glEnable(GL10.GL_TEXTURE_2D);

    renderMoon(gl, simulation.ship);
    renderShip(gl, simulation.ship, app);
    renderInvaders(gl, simulation.invaders);

    gl.glDisable(GL10.GL_TEXTURE_2D);
    renderBlocks(gl, simulation.blocks);

    gl.glDisable(GL10.GL_LIGHTING);
    renderShots(gl, simulation.shots);

    renderRays(gl, simulation.mRays);

    gl.glEnable(GL10.GL_TEXTURE_2D);
    renderExplosions(gl, simulation.explosions);

    gl.glDisable(GL10.GL_CULL_FACE);
    gl.glDisable(GL10.GL_DEPTH_TEST);

    spriteBatch.setProjectionMatrix(viewMatrix);
    spriteBatch.setTransformMatrix(transformMatrix);
    spriteBatch.begin();
    if (simulation.ship.lives != lastLives || simulation.score != lastScore || simulation.wave != lastWave) {
      status = "lives: " + simulation.ship.lives + " wave: " + simulation.wave + " score: " + simulation.score;
      lastLives = simulation.ship.lives;
      lastScore = simulation.score;
      lastWave = simulation.wave;
    }
    spriteBatch.enableBlending();
    spriteBatch.setBlendFunction(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
    font.draw(spriteBatch, status, 0, 320);
    spriteBatch.end();

    invaderAngle += app.getGraphics().getDeltaTime() * 90;
    if (invaderAngle > 360) invaderAngle -= 360;
  }

  private void renderBackground (GL10 gl) {
    // TODO: Eliminate fixed number ortho projection.
    //viewMatrix.setToOrtho2D(0, 0, 400, 320);
    viewMatrix.setToOrtho2D(0, 0, 480, 320);
    spriteBatch.setProjectionMatrix(viewMatrix);
    spriteBatch.setTransformMatrix(transformMatrix);
    spriteBatch.begin();
    spriteBatch.disableBlending();
    spriteBatch.setColor(Color.WHITE);
    spriteBatch.draw(backgroundTexture, 0, 0, 480, 320, 0, 0, 512, 512, false, false);
    spriteBatch.end();
  }

  private void renderEarth (GL10 gl) {
    // TODO: Eliminate fixed number ortho projection.
    final int TEXTURE_SIZE = 128;
    viewMatrix.setToOrtho2D(0, 0, 480, 320);
    spriteBatch.setProjectionMatrix(viewMatrix);
    spriteBatch.setTransformMatrix(transformMatrix);
    spriteBatch.begin();
    spriteBatch.disableBlending();
    spriteBatch.setColor(Color.WHITE);
    spriteBatch.draw(earthTexture, 288, 48, TEXTURE_SIZE, TEXTURE_SIZE, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, false, false);
    spriteBatch.end();
  }

  final Vector3 dir = new Vector3();

  private void setProjectionAndCameraAugmentedReality(Graphics graphics, Simulation simulation, Application app) {

    //camera.position.set(0, 6, 2);
    //camera.direction.set(0, 0, -4).sub(camera.position).nor();

    //camera.rotate(simulation.getPitch()   , 1 , 0 , 0);
    //camera.rotate(simulation.getRoll()    , 0 , 1 , 0);
    //camera.rotate(simulation.getAzimuth() , 0 , 0 , 1);

    // Note that these are taken from StackOverflow:
    // http://stackoverflow.com/questions/5274514/how-do-i-use-the-android-compass-orientation-to-aim-an-opengl-camera
    camera.position.set(0, 1, 2);
    camera.direction.set(0, 0, 1);
    camera.up.set(0, 1, 0);

    camera.rotate(simulation.getAzimuth() , 0 , 1 , 0);
    Vector3 pivot = camera.direction.cpy().crs(camera.up);

    camera.rotate(simulation.getPitch(), pivot.x, pivot.y, pivot.z);
    camera.rotate(simulation.getRoll(), camera.direction.x, camera.direction.y, camera.direction.z);

    camera.update();
    camera.apply(Gdx.gl10);
  }

  private void setProjectionAndCamera (Graphics graphics, Ship ship, Application app) {
    //camera.position.set(ship.position.x, 6, 2);

    camera.position.set(0, 6, 2);
    camera.direction.set(ship.position.x, 0, -4).sub(camera.position).nor();

    camera.update();
    camera.apply(Gdx.gl10);
  }

  float[] direction = {1, 0.5f, 0, 0};

  private void setLighting (GL10 gl) {
    gl.glEnable(GL10.GL_LIGHTING);
    gl.glEnable(GL10.GL_LIGHT0);
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, direction, 0);
    gl.glEnable(GL10.GL_COLOR_MATERIAL);
  }

  private void renderMoon (GL10 gl, Ship ship) {
    moonTexture.bind();
    gl.glPushMatrix();
    final float MOON_RADIUS = 10.0f;
    final float MOON_SCALE  = MOON_RADIUS;

    //gl.glTranslatef(ship.position.x, ship.position.y - MOON_RADIUS, ship.position.z);
    gl.glTranslatef(0.0f, ship.position.y - MOON_RADIUS, 0.0f);
    gl.glScalef(MOON_SCALE, MOON_SCALE, MOON_SCALE);
    //gl.glRotatef(45 * (-app.getInput().getAccelerometerY() / 5), 0, 0, 1);
    //gl.glRotatef(180, 0, 1, 0);

    moonMesh.render(GL10.GL_TRIANGLES);
    gl.glPopMatrix();
  }

  private void renderShip (GL10 gl, Ship ship, Application app) {
    if (ship.isExploding) return;

    shipTexture.bind();
    gl.glPushMatrix();
    gl.glTranslatef(ship.position.x, ship.position.y, ship.position.z);
    gl.glRotatef(45 * (-app.getInput().getAccelerometerY() / 5), 0, 0, 1);
    gl.glRotatef(180, 0, 1, 0);
    shipMesh.render(GL10.GL_TRIANGLES);
    gl.glPopMatrix();
  }

  private void renderInvaders (GL10 gl, ArrayList<Invader> invaders) {
    invaderTexture.bind();
    for (int i = 0; i < invaders.size(); i++) {
      Invader invader = invaders.get(i);
      gl.glPushMatrix();
      gl.glTranslatef(invader.position.x, invader.position.y, invader.position.z);
      gl.glRotatef(invaderAngle, 0, 1, 0);
      invaderMesh.render(GL10.GL_TRIANGLES);
      gl.glPopMatrix();
    }
  }

  private void renderBlocks (GL10 gl, ArrayList<Block> blocks) {
    gl.glEnable(GL10.GL_BLEND);
    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    gl.glColor4f(0.2f, 0.2f, 1, 0.7f);
    for (int i = 0; i < blocks.size(); i++) {
      Block block = blocks.get(i);
      gl.glPushMatrix();
      gl.glTranslatef(block.position.x, block.position.y, block.position.z);
      blockMesh.render(GL10.GL_TRIANGLES);
      gl.glPopMatrix();
    }
    gl.glColor4f(1, 1, 1, 1);
    gl.glDisable(GL10.GL_BLEND);
  }

  private void renderShots (GL10 gl, ArrayList<Shot> shots) {
    gl.glColor4f(1, 1, 0, 1);
    for (int i = 0; i < shots.size(); i++) {
      Shot shot = shots.get(i);
      gl.glPushMatrix();
      gl.glTranslatef(shot.position.x, shot.position.y, shot.position.z);
      shotMesh.render(GL10.GL_TRIANGLES);
      gl.glPopMatrix();
    }
    gl.glColor4f(1, 1, 1, 1);
  }

  private void renderRays (GL10 gl, ArrayList<RayShot> rays) {
    gl.glColor4f(1, 0, 1, 1);
    for (int i = 0; i < rays.size(); i++) {
      RayShot ray = rays.get(i);
      gl.glPushMatrix();
      gl.glTranslatef(ray.position.x, ray.position.y, ray.position.z);
      rayMesh.render(GL10.GL_TRIANGLES);
      gl.glPopMatrix();
    }
    gl.glColor4f(1, 1, 1, 1);
  }

  private void renderExplosions (GL10 gl, ArrayList<Explosion> explosions) {
    gl.glEnable(GL10.GL_BLEND);
    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    explosionTexture.bind();
    for (int i = 0; i < explosions.size(); i++) {
      Explosion explosion = explosions.get(i);
      gl.glPushMatrix();
      gl.glTranslatef(explosion.position.x, explosion.position.y, explosion.position.z);
      explosionMesh.render(GL10.GL_TRIANGLE_FAN, (int)((explosion.aliveTime / Explosion.EXPLOSION_LIVE_TIME) * 15) * 4, 4);
      gl.glPopMatrix();
    }
    gl.glDisable(GL10.GL_BLEND);
  }

  ////////////////////////////////////////
  // Utility
  ////////////////////////////////////////
  public Camera getCamera() {
    return camera;
  }

  public void unproject(Vector3 touchLocation) {
    Vector3 newLocation = new Vector3(touchLocation);
    camera.unproject(touchLocation);
    //Gdx.app.log(TAG, String.format("unproject from: (%s) to: (%s)",
    //                               newLocation.toString(),
    //                               touchLocation.toString()
    //                               ));
  }

  public void dispose () {
    spriteBatch.dispose();
    shipTexture.dispose();
    invaderTexture.dispose();
    backgroundTexture.dispose();
    explosionTexture.dispose();
    font.dispose();
    explosionMesh.dispose();
    shipMesh.dispose();
    invaderMesh.dispose();
    shotMesh.dispose();
    blockMesh.dispose();
  }
}
