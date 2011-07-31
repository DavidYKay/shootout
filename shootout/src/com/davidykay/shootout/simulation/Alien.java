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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class Alien {
  private static final String TAG = "Alien";
  private static final boolean DEBUG = false;

  //public static float ALIEN_RADIUS = 0.75f;
  public static float ALIEN_RADIUS = 1.0f;
  public static float ALIEN_VELOCITY = 1;
  public static int ALIEN_POINTS = 50;
  public static int SHOT_POINTS = 10;

  public final static int STATE_MOVE_LEFT = 0;
  public final static int STATE_MOVE_DOWN = 1;
  public final static int STATE_MOVE_RIGHT = 2;

  public final Vector3 position = new Vector3();
  public int state = STATE_MOVE_LEFT;
  public boolean wasLastStateLeft = true;
  public float movedDistance = Simulation.PLAYFIELD_MAX_X / 2;;

  public Alien (Vector3 position) {
    this.position.set(position);
  }

  public void update (float delta, float speedMultiplier) {
    movedDistance += delta * ALIEN_VELOCITY * speedMultiplier;
    if (state == STATE_MOVE_LEFT) {
      position.x -= delta * ALIEN_VELOCITY * speedMultiplier;
      if (movedDistance > Simulation.PLAYFIELD_MAX_X) {
        state = STATE_MOVE_DOWN;
        movedDistance = 0;
        wasLastStateLeft = true;
      }
    }
    if (state == STATE_MOVE_RIGHT) {
      position.x += delta * ALIEN_VELOCITY * speedMultiplier;
      if (movedDistance > Simulation.PLAYFIELD_MAX_X) {
        state = STATE_MOVE_DOWN;
        movedDistance = 0;
        wasLastStateLeft = false;
      }
    }
    if (state == STATE_MOVE_DOWN) {
      position.z += delta * ALIEN_VELOCITY * speedMultiplier;
      if (movedDistance > 1) {
        if (wasLastStateLeft)
          state = STATE_MOVE_RIGHT;
        else
          state = STATE_MOVE_LEFT;
        movedDistance = 0;
      }
    }

    if (DEBUG) {
      Gdx.app.log(TAG, String.format("alien moved to: (%s)",
                                     position.toString()));
    }
  }
}
