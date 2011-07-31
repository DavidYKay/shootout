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

package com.davidykay.shootout;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.davidykay.shootout.screens.GameLoop;
import com.davidykay.shootout.screens.GameOver;
import com.davidykay.shootout.screens.MainMenu;
import com.davidykay.shootout.screens.Screen;

public class ShootOut implements ApplicationListener {
  public static final String BACKGROUND = "data/starfield512.png";
  public static final String TITLE = "data/title.png";

  public static final String FONT_FNT = "data/font16.fnt";
  public static final String FONT_PNG = "data/font16.png";
  public static final String FONT10_FNT = "data/font10.fnt";
  public static final String FONT10_PNG = "data/font10.png";

  /** flag indicating whether we were initialized already **/
  private boolean isInitialized = false;

  /** the current screen **/
  private Screen screen;

  @Override public void dispose () {

  }

  @Override public void render () {
    Application app = Gdx.app;

    // update the screen
    screen.update(app);

    // render the screen
    screen.render(app);

    // when the screen is done we change to the
    // next screen
    if (screen.isDone()) {
      // dispose the current screen
      screen.dispose();

      // if this screen is a main menu screen we switch to
      // the game loop
      if (screen instanceof MainMenu)
        screen = new GameLoop(app);
      else
        // if this screen is a game loop screen we switch to the
        // game over screen
        if (screen instanceof GameLoop)
          screen = new GameOver(app);
        else
          // if this screen is a game over screen we switch to the
          // main menu screen
          if (screen instanceof GameOver) screen = new MainMenu(app);
    }
  }

  @Override public void resize (int width, int height) {

  }

  @Override public void create () {
    if (!isInitialized) {
      screen = new MainMenu(Gdx.app);
      Music music = Gdx.audio.newMusic(Gdx.files.getFileHandle("data/trollface.ogg", FileType.Internal));
      music.setLooping(true);
      music.play();
      isInitialized = true;
    }
  }

  @Override
  public void pause() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub
    System.out.println("resume");
  }
}
