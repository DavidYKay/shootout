package com.davidykay.shootout;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class ShootOutDesktop {
  public static void main (String[] argv) {
    new JoglApplication(new ShootOut(), "Hello World", 800, 480, false);
  }
}
