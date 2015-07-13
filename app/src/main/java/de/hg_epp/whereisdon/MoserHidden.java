package de.hg_epp.whereisdon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Outro
 * <p/>
 * (c) 2015 Jan Zartmann
 * (c) 2015 Christian Oder
 */
public class MoserHidden extends ActionBarActivity implements Animation.AnimationListener {

    private ImageView moser;

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
        setContentView(R.layout.moser_hidden);
        moser = (ImageView) findViewById(R.id.moser);
        TextView moser_hidden = (TextView) findViewById(R.id.moser_text);
        startMoserAnimation();
        moser_hidden.setMovementMethod(new ScrollingMovementMethod());
    }

    //starts the animation of Don
    private void startMoserAnimation() {
        Animation upDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_down_don);
        upDown.setRepeatCount(Animation.INFINITE);
        upDown.setRepeatMode(Animation.REVERSE);
        upDown.setAnimationListener(this);
        moser.setVisibility(View.VISIBLE);
        moser.startAnimation(upDown);
    }

    // start a fight against a level 25 Moser. Cause you know, he's freaking out, hence the lvl 25!
    public void getAngry(View unused){
        Intent startAct = new Intent(this, FightEngine.class);

        startAct.setAction(Intent.ACTION_SEND);
        startAct.putExtra(Intent.EXTRA_SUBJECT, "CreateFight");
        startAct.putExtra(Intent.EXTRA_TITLE, "Moser");
        startAct.setType("text/plain");
        startAct.putExtra(Intent.EXTRA_TEXT, Integer.toString(625));
        startAct.putExtra(Intent.EXTRA_UID, "Mo");
        startAct.putExtra(Intent.EXTRA_SHORTCUT_NAME, Integer.toString(ResourceManager.getMapID()));
        startAct.putExtra(Intent.EXTRA_REFERRER_NAME, Integer.toString(7));
        startAct.putExtra(Intent.EXTRA_BCC, "true");
        finish();
        this.startActivity(startAct);
    }

    // empty method needed for the Animation Listener
    @Override
    public void onAnimationStart(Animation animation) {

    }

    // restart the animation when it ends (Androids Loop function has some problems)
    @Override
    public void onAnimationEnd(Animation animation) {
        startMoserAnimation();
    }

    // empty method needed for the Animation Listener
    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}