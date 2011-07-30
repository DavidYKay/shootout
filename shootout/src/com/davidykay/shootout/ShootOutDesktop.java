package com.davidykay.shootout;

import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.davidykay.shootout.screens.ShootOut;

public class ShootOutDesktop {
  public static void main (String[] argv) {
    new JoglApplication(new GdxInvaders(), "Hello World", 800, 480, false);
  }
}
