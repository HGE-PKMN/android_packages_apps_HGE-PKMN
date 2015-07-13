package de.hg_epp.whereisdon;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

/**
 * Introduction Text to display our Story
 *
 * (c) 2015 Jan Zartmann
 * (c) 2015 Christian Oder
 */

public class Introduction extends ActionBarActivity {

    // make our App Fullscreen, no Matter if Window is focused or not
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
        //method sets the text and makes it scrollable
        TextView intro = (TextView) findViewById(R.id.intro_text);
        intro.setMovementMethod(new ScrollingMovementMethod());
    }

    // start the Game (listened to our feedback by the other team)
    public void resumeToMenu(View unused) {
        Intent startAct = new Intent(this, TMXTiledMapDigital.class);
        ResourceManager.setMapID(0);
        finish();
        this.startActivity(startAct);
    }
}