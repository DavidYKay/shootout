package com.davidykay.shootout.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class RayShot {
  private static final String TAG = "RayShot";
  private static float PLAYER_SHOT_VELOCITY = 10;
  private static float ALIEN_SHOT_VELOCITY = 5;

  private static float PLAYER_SHOT_RADIUS = 1;
  private static float  ALIEN_SHOT_RADIUS = 2;

  /** Speed which our shots move at. */
  private float shotVelocity;
  public float radius;
  public final Vector3 position = new Vector3();
  /** A unit vector which shows our current direction. */
  public final Vector3 direction = new Vector3();
  public boolean isInvaderShot;
  public boolean hasLeftField = false;

  public RayShot (Ray ray, boolean isInvaderShot) {
    this(ray.origin, ray.direction, isInvaderShot);
  }

  public RayShot (Vector3 position, Vector3 direction, boolean isInvaderShot) {
    this.position.set(position);
    this.direction.set(direction);
    this.isInvaderShot = isInvaderShot;
    this.shotVelocity = isInvaderShot ? ALIEN_SHOT_VELOCITY : PLAYER_SHOT_VELOCITY;
    this.radius       = isInvaderShot ? ALIEN_SHOT_RADIUS : PLAYER_SHOT_RADIUS;
  }

  public void update (float delta) {
    position.x += direction.x * shotVelocity * delta;
    position.y += direction.y * shotVelocity * delta;
    position.z += direction.z * shotVelocity * delta;

    Gdx.app.log(TAG, String.format("rayShot moving to: (%f, %f, %f)",
                             position.x,
                             position.y,
                             position.z));

    if (position.z > Simulation.PLAYFIELD_MAX_Z) hasLeftField = true;
    if (position.z < Simulation.PLAYFIELD_MIN_Z) hasLeftField = true;
  }
}
