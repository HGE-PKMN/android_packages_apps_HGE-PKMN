package de.hg_epp.whereisdon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;


public class FightEngine extends ActionBarActivity {
    /**
     * Engine Idea
     * @author Christian Schechter
     * @author Jan Zartmann
     * @author Christian Oder
     * https://developer.android.com/training/system-ui/immersive.html
     * Implementation of the Google Non-Sticky Immersive Mode
     */

    public static final String PREFS_NAME = "WIDPrefs";
    private int n;
    private int l;
    private double a;
    private double u;
    private double t;
    private double hp;
    private int WBType;
    private String[] attacks;
    private String[] sayings;
    private Activity mActivity;

    public void prepareFight(int type){
        WBType = type;
        WBType = type;
        switch (type){
            case 1:
                //Type Literatur
                u = 1.2;
                t = 0.8;
                break;
            case 2:
                //Type Mathe
                u= 0.8;
                t = 1.2;
                break;
            case 3:
                //Type Natur
                u = 1;
                t = 1;
                break;
        }

        setWBTPic();
        increaseN();
        getLevel();
        health();
        getArrays();
        setText();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fight_layout);
    }

    @Override
    protected void onResume(){
        super.onResume();
        //we are going to call a Dialog in here later, to select your Webertron Type
        prepareFight(1);
    }

    private void increaseN(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        n = settings.getInt("amount_games", 0);
        if (n<225)
            n++;
        editor.putInt("amount_games", n);
/*        return n;*/
    }

    private void getLevel(){
        l = (int) Math.sqrt(n);
        Log.e("WID", "l:" + l);
/*        return l;*/
    }

    private double attack_power(){
        Random r = new Random();
        int Low = 5;
        int High = 151;
        double R = r.nextInt(High-Low) + Low / 100;
        a = 250 * Math.sqrt(l) * R * u;
        return a;
    }

    private void health(){
        hp = 1000 * t * Math.sqrt(l);
/*        return hp;*/
    }

    private void fight(){

    }

    private void getArrays(){
        Resources res = getResources();
        attacks = res.getStringArray(R.array.attacks);
        sayings = res.getStringArray(R.array.sayings);
    }

    private void setText(){
        Random r = new Random();
        int Low = 0;
        int High1 = attacks.length;
        int High2 = sayings.length;
        int R1 = r.nextInt(High1-Low) + Low;
        int R2 = r.nextInt(High2-Low) + Low;
        Button attack_button = (Button)findViewById(R.id.attack_button);
        TextView sayingsTV = (TextView) findViewById(R.id.textfield);
        attack_button.setText(attacks[R1]);
        sayingsTV.setText(sayings[R2]);
    }

    private void setWBTPic(){
        ImageView wbt= (ImageView) findViewById(R.id.webertron_1);
        wbt.setImageResource(R.drawable.wbt_2);
        ImageView wbt2= (ImageView) findViewById(R.id.webertron_2);
        wbt2.setImageResource(R.drawable.wbt_3);
    }

    public void fight(View unused){
        //code to fight in here.
    }

    public void escape(View unused){
        //temp code to test the prepare Fight method
        prepareFight(1);
    }
}
