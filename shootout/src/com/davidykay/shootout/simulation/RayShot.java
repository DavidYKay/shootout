package com.davidykay.shootout.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class RayShot {
  private static final String TAG = "RayShot";

  public static float SHOT_VELOCITY = 10;
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
  }

  public void update (float delta) {
    //if (isInvaderShot) {
    //  position.z += SHOT_VELOCITY * delta;
    //}
    //else
    //  position.z -= SHOT_VELOCITY * delta;

    position.x += direction.x * SHOT_VELOCITY * delta;
    position.y += direction.y * SHOT_VELOCITY * delta;
    position.z += direction.z * SHOT_VELOCITY * delta;

    //position.x += direction.x * delta;
    //position.y += direction.y * delta;
    //position.z += direction.z * delta;

    Gdx.app.log(TAG, String.format("rayShot moving to: (%f, %f, %f)",
                             position.x,
                             position.y,
                             position.z));

    if (position.z > Simulation.PLAYFIELD_MAX_Z) hasLeftField = true;
    if (position.z < Simulation.PLAYFIELD_MIN_Z) hasLeftField = true;
  }
}
