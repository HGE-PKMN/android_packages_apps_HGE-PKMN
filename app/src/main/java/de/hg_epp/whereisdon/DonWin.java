package de.hg_epp.whereisdon;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
 */
public class DonWin extends ActionBarActivity implements Animation.AnimationListener {

    private ImageView don;
    private TextView winMes2;

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
        winMes2 = (TextView) findViewById(R.id.outro);
        setPic();
        setText();
        startDonAnimation();
    }

    public void setPic() {
        don.setImageResource(R.drawable.don);

    }

    public void setText() {
        winMes2.setText(setRaw());
    }

    //method reads the Raw
    private String setRaw() {
        InputStream inputStream = getResources().openRawResource(R.raw.outro);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int j;
        try {
            j = inputStream.read();
            while (j != -1) {
                byteArrayOutputStream.write(j);
                j = inputStream.read();
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
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