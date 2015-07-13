package de.hg_epp.whereisdon;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Outro
 * <p/>
 * (c) 2015 Jan Zartmann
 * (c) 2015 Christian Oder
 */
public class DonWin extends ActionBarActivity implements Animation.AnimationListener {

    private ImageView don;

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
        setContentView(R.layout.don_win);
        don = (ImageView) findViewById(R.id.donWin);
        startDonAnimation();
        TextView don_text = (TextView) findViewById(R.id.outro);
        don_text.setMovementMethod(new ScrollingMovementMethod());
    }

    //starts the animation of Don
    private void startDonAnimation() {
        Animation upDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_down_don);
        upDown.setRepeatCount(Animation.INFINITE);
        upDown.setRepeatMode(Animation.REVERSE);
        upDown.setAnimationListener(this);
        don.setVisibility(View.VISIBLE);
        don.startAnimation(upDown);
    }


    public void returnToMenu(View unused){
        Intent startAct = new Intent(this, MainActivity.class);
        Toast.makeText(this, getString(R.string.finished_game), Toast.LENGTH_LONG).show();
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
        startDonAnimation();
    }

    // empty method needed for the Animation Listener
    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}