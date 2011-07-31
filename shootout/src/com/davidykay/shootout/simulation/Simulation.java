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

package com.davidykay.shootout.simulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Simulation {

  public final static float ENEMY_ROWS    = 4;
  public final static float ENEMY_COLUMNS = 8;

  public final static float PLAYFIELD_MIN_X = -14;
  public final static float PLAYFIELD_MAX_X = 14;
  public final static float PLAYFIELD_MIN_Z = -15;
  public final static float PLAYFIELD_MAX_Z = 2;

  public final static float PLAYFIELD_MIN_Y = -15;
  public final static float PLAYFIELD_MAX_Y = 2;

  public final static float MAX_SHOTS = 4;

  private static final String TAG = "Simulation";
  private static final boolean DEBUG = false;

  public ArrayList<Invader> invaders     = new ArrayList<Invader>();
  public ArrayList<Block> blocks         = new ArrayList<Block>();
  public ArrayList<Explosion> explosions = new ArrayList<Explosion>();
  public Ship ship;

  //public ArrayList<RayShot> mRays = new ArrayList<RayShot>();
  public ArrayList<RayShot> mShipRays = new ArrayList<RayShot>();
  public ArrayList<RayShot> mAlienRays = new ArrayList<RayShot>();
  public transient SimulationListener listener;
  public float multiplier = 1;
  public int score;
  public int wave = 1;

  private ArrayList<Explosion> removedExplosions = new ArrayList<Explosion>();
  private ArrayList<RayShot> removedRays = new ArrayList<RayShot>();

  //////////////////////////////////////////////////////////////////////
  // Initialization
  //////////////////////////////////////////////////////////////////////

  public Simulation () {
    populate();
  }

  private void populate () {
    ship = new Ship();

    Random random = new Random();

    final float ROW_SIZE = 2.5f;
    final float COLUMN_SIZE = 2.5f;

    for (int row = 0; row < ENEMY_ROWS; row++) {
      for (int column = 0; column < ENEMY_COLUMNS; column++) {
        Invader invader = new Invader(
            new Vector3(
                -PLAYFIELD_MAX_X / 2 + column * COLUMN_SIZE,
                random.nextInt(6),
                PLAYFIELD_MIN_Z + row * ROW_SIZE
            )
        );
        invaders.add(invader);
      }
    }

    //for (int shield = 0; shield < 3; shield++) {
    //  blocks.add(new Block(new Vector3(-10 + shield * 10 - 1, 0, -2)));
    //  blocks.add(new Block(new Vector3(-10 + shield * 10 - 1, 0, -3)));
    //  blocks.add(new Block(new Vector3(-10 + shield * 10 + 0, 0, -3)));
    //  blocks.add(new Block(new Vector3(-10 + shield * 10 + 1, 0, -3)));
    //  blocks.add(new Block(new Vector3(-10 + shield * 10 + 1, 0, -2)));
    //}
  }

  //////////////////////////////////////////////////////////////////////
  // Game Logic
  //////////////////////////////////////////////////////////////////////

  public void update (float delta) {
    synchronized (mShipRays) {
      ship.update(delta);
      updateInvaders(delta);
      updateRays(delta);
      updateExplosions(delta);
      checkShipCollision();
      checkInvaderCollision();
      //checkBlockCollision();
      checkNextLevel();
      //synchronized (mShipRays) {
      //}
    }
  }

  private void updateInvaders (float delta) {
    for (int i = 0; i < invaders.size(); i++) {
      Invader invader = invaders.get(i);
      invader.update(delta, multiplier);
    }
  }

  private void updateRays (float delta) {
    removedRays.clear();

    ArrayList<RayShot> rays = getAllRays();

    for (RayShot ray : rays) {
      // Move.
      ray.update(delta);
      // If they've left the building, remove.
      if (ray.hasLeftField) removedRays.add(ray);
    }

    // Clear up removed
    for (RayShot ray : removedRays) {
      if (ray.isInvaderShot) {
        mAlienRays.remove(ray);
      } else {
        mShipRays.remove(ray);
      }
    }

    // Check player shots against computer shots.
rays:
    for (RayShot ray: mShipRays) {
      for (RayShot enemyRay: mAlienRays) {
        if (enemyRay.position.dst(ray.position) < ray.radius + enemyRay.radius) {
          // Boom!
          mShipRays.remove(ray);
          mAlienRays.remove(enemyRay);
          explosions.add(new Explosion(enemyRay.position));
          if (listener != null) listener.explosion();
          continue rays;
        }
      }
    }

    // UFOs shoot!
    if (Math.random() < 0.01 * multiplier && invaders.size() > 0) {
      int index = (int)(Math.random() * (invaders.size() - 1));
      Vector3 position = invaders.get(index).position;
      Vector3 direction = new Vector3(0,0,0).sub(position).nor();
      RayShot shot = new RayShot(position,
                                 direction,
                                 true);
      mAlienRays.add(shot);
      if (listener != null) listener.shot();
    }
  }

  public void updateExplosions (float delta) {
    removedExplosions.clear();
    for (int i = 0; i < explosions.size(); i++) {
      Explosion explosion = explosions.get(i);
      explosion.update(delta);
      if (explosion.aliveTime > Explosion.EXPLOSION_LIVE_TIME) removedExplosions.add(explosion);
    }

    for (int i = 0; i < removedExplosions.size(); i++)
      explosions.remove(removedExplosions.get(i));
  }

  private void checkInvaderCollision () {
    //if (mRays.isEmpty()) return;
    if (mShipRays.isEmpty()) return;

    // Brute force collision detection.
invaders:
    for (int j = 0; j < invaders.size(); j++) {
      Invader invader = invaders.get(j);
shots:
      for (RayShot ray : mShipRays) {
        if (invader.position.dst(ray.position) < Invader.INVADER_RADIUS) {
          mShipRays.remove(ray);
          invaders.remove(invader);
          explosions.add(new Explosion(invader.position));
          if (listener != null) listener.explosion();
          score += Invader.INVADER_POINTS;

          // Go to the next invader.
          break invaders;
        }
      }
    }
  }

  /**
   * See if the player was hit.
   */
  private void checkShipCollision () {
    // Check for collision with rays
    for (RayShot ray : mAlienRays) {
      if (ray.position.dst(ship.position) < Ship.SHIP_RADIUS) {
        ship.lives--;
        mAlienRays.remove(ray);
        ship.isExploding = true;
        explosions.add(new Explosion(ship.position));
        if (listener != null) listener.explosion();
        break;
      }
    }

    // Check for collision with ufos.
    for (int i = 0; i < invaders.size(); i++) {
      Invader invader = invaders.get(i);
      if (invader.position.dst(ship.position) < Ship.SHIP_RADIUS) {
        ship.lives--;
        invaders.remove(invader);
        ship.isExploding = true;
        explosions.add(new Explosion(invader.position));
        explosions.add(new Explosion(ship.position));
        if (listener != null) listener.explosion();
        break;
      }
    }
  }

  //private void checkBlockCollision () {
  //  //for (RayShot ray : mRays) {
  //  for (int i = 0; i < mRays.size(); i++) {
  //    RayShot ray = mRays.get(i);
  //    for (int j = 0; j < blocks.size(); j++) {
  //      Block block = blocks.get(j);
  //      if (block.position.dst(ray.position) < Block.BLOCK_RADIUS) {
  //        mRays.remove(ray);
  //        blocks.remove(block);
  //        break;
  //      }
  //    }
  //  }
  //}

  private void checkNextLevel () {
    if (invaders.size() == 0 && ship.lives > 0) {
      blocks.clear();
      //mRays.clear();
      mAlienRays.clear();
      mShipRays.clear();
      Vector3 shipPosition = ship.position;
      int lives = ship.lives;
      populate();
      ship.position.set(shipPosition);
      ship.lives = lives;
      multiplier += 0.1f;
      wave++;
    }
  }

  //////////////////////////////////////////////////////////////////////
  // Utility
  //////////////////////////////////////////////////////////////////////

  private ArrayList<RayShot>getAllRays() {
    ArrayList<RayShot> newList = new ArrayList<RayShot>(mAlienRays);
    newList.addAll(mShipRays);
    return newList;
  }

  //////////////////////////////////////////////////////////////////////
  // Player Input
  //////////////////////////////////////////////////////////////////////

  public void moveShipLeft (float delta, float scale) {
    if (ship.isExploding) return;

    ship.position.x -= delta * Ship.SHIP_VELOCITY * scale;
    if (ship.position.x < PLAYFIELD_MIN_X) ship.position.x = PLAYFIELD_MIN_X;

    //Gdx.app.log(TAG, String.format("moveShipLeft() to: (%f, %f, %f)",
    //                         ship.position.x,
    //                         ship.position.y,
    //                         ship.position.z));
  }

  public void moveShipRight (float delta, float scale) {
    if (ship.isExploding) return;

    ship.position.x += delta * Ship.SHIP_VELOCITY * scale;
    if (ship.position.x > PLAYFIELD_MAX_X) ship.position.x = PLAYFIELD_MAX_X;

    //Gdx.app.log(TAG, String.format("moveShipRight() to: (%f, %f, %f)",
    //                         ship.position.x,
    //                         ship.position.y,
    //                         ship.position.z));
  }

  public void tapRay(Ray ray) {
    Vector3 direction = new Vector3(0, 0, -1);
    RayShot vanilla = new RayShot(ray, false);
    RayShot custom  = new RayShot(
            ray.origin,
            direction,
            false
            );
    RayShot rayshot = vanilla;
    synchronized (mShipRays) {
      mShipRays.add(rayshot);
    }
    if (listener != null) listener.ray();
  }

  //////////////////////////////////////////////////////////////////////
  // Orientation Logic
  //////////////////////////////////////////////////////////////////////

  private LinkedList<Orientation> mOrientations = new LinkedList<Orientation>();
  private static final int MAX_ORIENTATIONS = 15;

  private float mAzimuth;
  private float mPitch  ;
  private float mRoll   ;

  private void averageOrientationValues() {
    float azimuth = 0.0f;
    float pitch   = 0.0f;
    float roll    = 0.0f;
    for (Orientation o : mOrientations) {
      azimuth += o.azimuth;
      pitch += o.pitch;
      roll += o.roll;
    }
    int count = mOrientations.size();
    mAzimuth = azimuth / count;
    mPitch   = pitch / count;
    mRoll    = roll / count;
  }

  /**
   * Add a given orientation to our queue.
   */
  public void addOrientation(Orientation orientation) {

    // Note that these are taken from StackOverflow:
    // http://stackoverflow.com/questions/5274514/how-do-i-use-the-android-compass-orientation-to-aim-an-opengl-camera
    mOrientations.offer(orientation);
    if (mOrientations.size() > MAX_ORIENTATIONS) {
      mOrientations.remove();
    }
    // TODO: Technically we don't care until we retrieve.
    // Average out our values
    averageOrientationValues();

    if (DEBUG) {
      Gdx.app.log(TAG, String.format("Orientation: (%s)",
                                     orientation.toString()));
    }
  }

  /**
   * Take orientation data from the device.
   */
  public void updateOrientation(
      float azimuth ,
      float pitch   ,
      float roll
      ) {
    Orientation orientation = new Orientation(
        -azimuth,
        -roll - 90,
        -pitch);
      //mAzimuth = azimuth;
      //mPitch   = pitch;
      //mRoll    = roll;
    addOrientation(orientation);
  }
  public float getAzimuth() {
    return mAzimuth;
  }
  public float getPitch() {
    return mPitch;
  }
  public float getRoll() {
    return mRoll;
  }

  public class Orientation {
    //public final Vector3 position = new Vector3(0,1.5f,0);
    /** Angle left or right of the vertical */
    public float azimuth = 0.0f;
    /** Angle above or below the horizon */
    public float pitch = 0.0f;
    /** Angle about the direction as defined by yaw and pitch */
    public float roll = 0.0f;

    public Orientation(float azimuth, float pitch, float roll) {
      this.azimuth = azimuth;
      this.pitch   = pitch;
      this.roll    = roll;
    }
    public String toString() {
      return String.format("(%f, %f, %f)",
                    roll,
                    pitch,
                    azimuth);
    }
  }
}
