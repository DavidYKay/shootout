package com.davidykay.shootout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.davidykay.shootout.screens.ShootOut;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class MainActivity extends AndroidApplication {
    /** Called when the activity is first created. */
  @Override public void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
    config.useWakelock = true;    
    initialize(new GdxInvaders(),config);
  }
}
