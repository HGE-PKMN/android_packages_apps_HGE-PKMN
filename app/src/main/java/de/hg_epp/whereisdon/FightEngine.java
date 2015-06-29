package de.hg_epp.whereisdon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FightEngine extends ActionBarActivity implements Animation.AnimationListener {
    /**
     * Engine Idea:
     *
     * @author Christian Schechter
     * with slight modifications of:
     * @author Christian Oder
     * <p/>
     * Code:
     * @author Christian Oder
     * @author Jan Zartmann
     * <p/>
     * https://developer.android.com/training/system-ui/immersive.html
     * Implementation of the Google Non-Sticky Immersive Mode
     */

    public static final String PREFS_NAME = "WIDPrefs";

    private String[] attacks;
    private String[] sayings;
    private ArrayList<Integer> mDrawableArray = new ArrayList<>();
    private Button attack_button;
    private TextView sayingsTV;
    private boolean trainerfight;
    private boolean button_locked;
    private int remainingFights;
    private int winsPlayer;
    private int winsTeacher;
    private int teacherWBT;
    private ImageView wbt_p;
    private ImageView wbt_t;
    private double player_lvl;
    private double teacher_lvl;
    private int teacher_won_fights;
    private String teacher_name;
    private String teacher_token;
    private int wbt_type_p;
    private int wbt_type_t;
    private double hp_p;
    private double hp_t;
    private View fake_view;

    // make our App Fullscreen
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // Initialize the ImageViews, TextViews and Buttons, create the fake_view and
    // unlock the attack button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fight_layout);

        wbt_p = (ImageView) findViewById(R.id.webertron_p);
        wbt_t = (ImageView) findViewById(R.id.webertron_t);


        // load animation upDown
        startWBTAnimation();

        attack_button = (Button) findViewById(R.id.attack_button);
        sayingsTV = (TextView) findViewById(R.id.textfield);
        // create some fake view to pass an argument to the escape method
        fake_view = findViewById(R.id.action_bar);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("WIDCheatCodeInput".equals(type)) {
                cheatSetN(intent); // Handle text being sent
            } else if ("WIDCheatCodeUpdate".equals(type)) {
                cheatAnswerN(); // Handle text being sent
            } else if ("CreateFight".equals(type)) {
                unlockButton();
                prepareFight(intent);
            }
        } else {
            Log.e("WID", "No Intent detected!");
        }
    }

    // override the action when pressing the back button to block escaping from a trainer fight.
    @Override
    public void onBackPressed() {
        escape(fake_view);
    }

    public void chooseWBT() {
        DialogFragment DialogFragment = new selectWBTDialog();
        DialogFragment.show(getFragmentManager(), "WBT");
    }

    // initially prepare for a fight, set the wins to 0 and the remaining fights to 3
    // (can be changed later). Also set trainer fight true (this allows us to easily implement wild
    // Webertron fights later on, then call prepareNextFight.
    public void prepareFight(Intent intent) {
        // I know it is bad sport to call android methods manually, but we need to be fullscreen
        // before calling the Dialog in prepareNextFight
        onWindowFocusChanged(true);
        getArrays();
        winsPlayer = 0;
        winsTeacher = 0;
        // set the remaining fights to 3 + 1 (4), cause we initially reduce it by 1,
        // so we only have 3 rounds
        remainingFights = 3 + 1;
        trainerfight = true;
        wbt_type_p = 0;
        wbt_type_t = 0;
        hp_p = 0;
        hp_t = 0;
        player_lvl = 0;
        teacher_lvl = 0;
        setTeacherWonFights(intent);
        setTeacherName(intent);
        setTeacherToken(intent);
        prepareNextFight();
    }

    // open Dialog to select Players Webertron, after that initialize the other parts
    // this happens in prepareNextFight2ndPart
    public void prepareNextFight() {
        chooseWBT();
    }

    // prepare for the next fight, reload the Webertron Images, set new Texts, and update the
    // HP and LVL Text View
    public void prepareNextFight2ndPart() {
        unlockButton();
        setWBTPic();
        setText();
        teacherWBT = 0;

        // Initialize all Values for the fight method
        player_lvl = getPlayerLevel();
        teacher_lvl = getTeacherLevel();
        wbt_type_p = getPlayerWBT();
        wbt_type_t = getTeacherWBT();
        hp_p = getHP(getT(wbt_type_p), player_lvl);
        hp_t = getHP(getT(wbt_type_t), teacher_lvl);
        lowerRemainingFights();
        setHP(hp_p, hp_t);
        setLVL(player_lvl, teacher_lvl);
        setWins(winsPlayer, winsTeacher);
    }

    // increases the players won games
    private void increaseN() {
        // max level is 20, so max amount of count games is 20² = 400
        if (getN() < 400) {
            setN(getN() + 1);
        }
    }

    //get N from the Shared Settings
    private int getN() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt("won_games", 0);
    }

    //store a new N to the Shared Settings
    private void setN(int n) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("won_games", n);
        editor.apply();
    }

    public void cheatAnswerN() {
        Intent sendIntent = new Intent();
        sendIntent.setComponent(new ComponentName("de.myself5.whereisdoncheats", "de.myself5.whereisdoncheats.MainActivity"));
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, Integer.toString(getN()));
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "WIDCheatCodeAnswer");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
        finish();
    }

    // this is our cheat method, to set our own level from the WID Cheat App
    public void cheatSetN(Intent intent) {
        int n = Integer.parseInt(intent.getStringExtra(Intent.EXTRA_TEXT));
        setN(n);
        finish();
    }

    // returns the Players current Level in dependence of the won games
    private double getPlayerLevel() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int nFights = settings.getInt("won_games", 1);
        return Math.sqrt(nFights * 1D);
    }

    private void setTeacherWonFights(Intent intent) {
        String levelS = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (!levelS.equals("")) {
            teacher_won_fights = Integer.parseInt(levelS);
        }
    }

    // returns the Teachers Level
    private double getTeacherLevel() {
        return Math.sqrt(teacher_won_fights * 1D);
    }

    // calculates the Webertrons HP in dependence of the type factor (u/atk_pwr), the players level
    // and a random factor between 0.85 and 1.15
    private double getAtkPwr(double atk_pwr, double level) {
        Random r = new Random();
        int Low = 85;
        // this calculates a number inside [85; 116) -> [85; 115]
        int High = 115 + 1;
        double R = (r.nextInt(High - Low) + Low) / 100D;
        double atkp;
        Log.e("WID", "R: " + R);
        atkp = (250 * Math.sqrt(level) * R * atk_pwr);
        Log.e("WID", "atkp: " + atkp);
        return atkp;
    }

    // calculates the Webertrons HP in dependence of the type factor (t/type) and the players level
    private double getHP(double type, double level) {
        return (1500D * type * Math.sqrt(level));
    }

    // loads the arrays from the strings.xml
    private void getArrays() {
        Resources res = getResources();
        attacks = res.getStringArray(R.array.attacks);
        sayings = res.getStringArray(R.array.sayings);
        mDrawableArray.add(R.drawable.wbt_1);
        mDrawableArray.add(R.drawable.wbt_2);
        mDrawableArray.add(R.drawable.wbt_3);
        mDrawableArray.add(R.drawable.wbt_4);
        mDrawableArray.add(R.drawable.wbt_5);
    }

    // randomly sets the Sayings and Attack Button Text
    private void setText() {
        Random r = new Random();
        int Low = 0;
        int High1 = attacks.length;
        int High2 = sayings.length;
        int R1 = r.nextInt(High1 - Low) + Low;
        int R2 = r.nextInt(High2 - Low) + Low;
        attack_button.setText(attacks[R1]);
        sayingsTV.setText(getTeacherName() + " " + sayings[R2]);
    }

    // sets the Sayings TextView text to R.string.fighting
    private void setFightingText() {
        sayingsTV.setText(getString(R.string.fighting));
    }

    // randomly loads a Webertron image to the ImageViews
    private void setWBTPic() {
        Random r = new Random();
        int Low = 0;
        int High = mDrawableArray.size();
        int R1 = r.nextInt(High - Low) + Low;
        int R2 = r.nextInt(High - Low) + Low;
        /*ImageView wbt1 = (ImageView) findViewById(R.id.webertron_1);*/
        wbt_p.setImageResource(mDrawableArray.get(R1));
        /*ImageView wbt2 = (ImageView) findViewById(R.id.webertron_2);*/
        //this ImageView needs to be mirrored
        wbt_t.setImageBitmap(flipImage(BitmapFactory.decodeResource(getResources(), mDrawableArray.get(R2)), 2));
    }

    // This allows us to mirror the Image so the Webertrons face each other
    // Source: http://shaikhhamadali.blogspot.de/2013/08/image-flipping-mirroring-in-imageview.html
    public Bitmap flipImage(Bitmap src, int type) {
        // create new matrix for transformation
        int FLIP_VERTICAL = 1;
        int FLIP_HORIZONTAL = 2;
        Matrix matrix = new Matrix();
        // if vertical
        if (type == FLIP_VERTICAL) {
            // y = y * -1
            matrix.preScale(1.0f, -1.0f);
        }
        // if horizontal
        else if (type == FLIP_HORIZONTAL) {
            // x = x * -1
            matrix.preScale(-1.0f, 1.0f);
            // unknown type
        } else {
            return null;
        }

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    // randomly assigns a Webertron Type to a teacher
    private int setTeacherWBTType() {
        Random r = new Random();
        int Low = 1;
        int High = 4;
        return r.nextInt(High - Low) + Low;
    }

    // returns the Webertron Type the Teacher got assigned randomly
    private int getTeacherWBT() {
        // if it's the first call after prepareNextFight set a random Webertron, else just return
        // the current value
        if (teacherWBT == 0) {
            teacherWBT = setTeacherWBTType();
        }
        return teacherWBT;
    }

    // returns the Webertron Type the Player selected
    private int getPlayerWBT() {
        return wbt_type_p;
    }

    public void fight(View unused) {
        if (!button_locked) {
            // atk has the random factor inside, so recalculate it every hit

            Log.e("WID", "wbt_type_p: " + wbt_type_p);
            Log.e("WID", "wbt_type_t: " + wbt_type_t);
            Log.e("WID", "player_lvl: " + player_lvl);
            Log.e("WID", "teacher_lvl: " + teacher_lvl);
            Log.e("WID", "getU(wbt_type_p): " + getU(wbt_type_p));
            Log.e("WID", "getU(wbt_type_t): " + getU(wbt_type_t));
            double atk_p = getAtkPwr(getU(wbt_type_p), player_lvl);
            double atk_t = getAtkPwr(getU(wbt_type_t), teacher_lvl);

            // actually fight
            // see if one has killed the other already
            // Teacher attacks first, when you PKM is dead you can't attack the teacher
            Log.e("WID", "P_ATK: " + atk_p);
            Log.e("WID", "T_ATK: " + atk_t);
            Log.e("WID", "P_HP: " + hp_p);
            Log.e("WID", "T_HP: " + hp_t);
            hp_p = hp_p - (int) atk_t;
            hp_t = hp_t - (int) atk_p;

            // wait 2 seconds and set Text, do this in an Async to not freeze the Animation
            // but set an boolean to block the button during the Waiting Time

            setFightingText();
            lockButton();
            new FightEngineAsyncWait().execute("");
        } else {
            Log.e("WID", "Attack button locked!");
        }
    }

    // updates the TextView with the Player and the Teachers Level
    private void setLVL(double player_lvl, double teacher_lvl) {
        TextView tvPlayerLVL = (TextView) findViewById(R.id.lvl_player);
        TextView tvTeacherLVL = (TextView) findViewById(R.id.lvl_teacher);
        tvPlayerLVL.setText(getString(R.string.LVL) + " " + Math.round(player_lvl));
        tvTeacherLVL.setText(getString(R.string.LVL) + " " + Math.round(teacher_lvl));
    }

    // updates the TextView with the Webertrons current HP
    private void setHP(double hp_p, double hp_t) {
        TextView tvPlayerHP = (TextView) findViewById(R.id.hp_player);
        TextView tvTeacherHP = (TextView) findViewById(R.id.hp_teacher);
        tvPlayerHP.setText(getString(R.string.HP) + " " + Math.round(hp_p));
        tvTeacherHP.setText(getString(R.string.HP) + " " + Math.round(hp_t));
    }

    // updates the TextView with the Players and Teachers current Wins
    private void setWins(int win_p, int win_t) {
        TextView tvWinsPlayer = (TextView) findViewById(R.id.winsPlayer);
        TextView tvWinsTeacher = (TextView) findViewById(R.id.winsTeacher);
        tvWinsPlayer.setText(getString(R.string.wins) + " " + getString(R.string.you) + " " + win_p);
        tvWinsTeacher.setText(getString(R.string.wins) + " " + getTeacherToken() + " " + win_t);
    }

    private void setTeacherName(Intent intent) {
        String nameS = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (!nameS.equals("")) {
            teacher_name = nameS;
        }
    }

    private void setTeacherToken(Intent intent){
        String tokenS = intent.getStringExtra(Intent.EXTRA_UID);
        if (!tokenS.equals("")) {
            teacher_token = tokenS;
        }
    }

    private String getTeacherName() {
        return teacher_name + ":";
    }

    private String getTeacherToken() {

        return teacher_token + ":";
    }

    // updates the TextView with the Remaining Fights
    private void lowerRemainingFights() {
        remainingFights--;
        TextView tvRemainingFights = (TextView) findViewById(R.id.remainingFights);
        tvRemainingFights.setText(getString(R.string.remainingFights) + " " + remainingFights);
    }


    // on Click Method for the Escape button.
    // Doesn't work if Player is fighting against a Teacher for the first time.
    // Is also called by the other classes to end the game (using the fake_view as a parameter)
    public void escape(View unused) {
        if (!trainerfight) {
            //close the activity and return to the map
            finish();
        } else {
            Toast.makeText(this, getString(R.string.cant_escape_in_trainerfight), Toast.LENGTH_LONG).show();
        }
    }

    // returns the U value of each Pokemon Type (it affects the max ATTK Power of each Webertron)
    public double getU(int type) {
        double u = 0;
        switch (type) {
            case 1:
                //Type Literatur
                u = 1.2D;
                break;
            case 2:
                //Type Mathe
                u = 0.8D;
                break;
            case 3:
                //Type Natur
                u = 1.0D;
                break;
        }
        return u;
    }

    // returns the T value of each Pokemon Type (it affects the max HP of each Webertron)
    public double getT(int type) {
        double t = 0;
        switch (type) {
            case 1:
                //Type Literatur
                t = 0.8D;
                break;
            case 2:
                //Type Mathe
                t = 1.2D;
                break;
            case 3:
                //Type Natur
                t = 1.0D;
                break;
        }
        return t;
    }

    // Show some Toast message when the Player wins,
    // and close the FightEngine when the Player won 2 (out of 3) matches
    private void winPlayer() {
        winsPlayer++;
        increaseN();
        if (winsPlayer == 2) {
            Toast.makeText(this, getString(R.string.duel_won), Toast.LENGTH_LONG).show();
            trainerfight = false;
            escape(fake_view);
        } else {
            Toast.makeText(this, getString(R.string.fight_won), Toast.LENGTH_LONG).show();
            prepareNextFight();
        }
    }

    // Show some Toast message when the Teacher wins,
    // and close the FightEngine when the Teacher won 2 (out of 3) matches
    private void winTeacher() {
        winsTeacher++;
        if (winsTeacher == 2) {
            Toast.makeText(this, getString(R.string.duel_lost), Toast.LENGTH_LONG).show();
            trainerfight = false;
            escape(fake_view);
        } else {
            Toast.makeText(this, getString(R.string.fight_lost), Toast.LENGTH_LONG).show();
            prepareNextFight();
        }
    }

    // empty method needed for the Animation Listener
    @Override
    public void onAnimationStart(Animation animation) {

    }

    // restart the animation when it ends (Androids Loop function has some problems)
    @Override
    public void onAnimationEnd(Animation animation) {
        startWBTAnimation();
    }

    // empty method needed for the Animation Listener
    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    // make the Webertrons bounce up and down
    private void startWBTAnimation() {
        Animation upDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_down);
        upDown.setRepeatCount(Animation.INFINITE);
        upDown.setRepeatMode(Animation.REVERSE);
        upDown.setAnimationListener(this);
        wbt_p.setVisibility(View.VISIBLE);
        wbt_t.setVisibility(View.VISIBLE);
        wbt_p.startAnimation(upDown);
        wbt_t.startAnimation(upDown);
    }

    // lock the Attack button
    public void lockButton() {
        button_locked = true;
    }

    // unlock the Attack button
    public void unlockButton() {
        button_locked = false;
    }

    /**
     * Just a little tool to sleep for 2 Seconds without blocking the View Thread for FightEngine
     * Created by Christian Oder on 26/06/2015.
     */
    public class FightEngineAsyncWait extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                //sleep
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("WID", "doinBackground");
            //just to have a return, actually it's unused in the onPostExecute
            return "good Morning";
        }

        // unlock the button and update the HP TextView and set the saying and button text
        @Override
        protected void onPostExecute(String unused) {
            // randomly choose the order of the attacks
            // Either Player Webertron attacks first or Teacher Webertron does so
            Random random = new Random();
            if (random.nextBoolean()) {
                if (hp_p < 1) {
                    winTeacher();
                } else {
                    if (hp_t < 1) {
                        winPlayer();
                    } else {
                        Log.e("WID", "onPostExecute");
                        unlockButton();
                        setHP(hp_p, hp_t);
                        setText();
                    }
                }
            } else {
                if (hp_t < 1) {
                    winPlayer();
                } else {
                    if (hp_p < 1) {
                        winTeacher();
                    } else {
                        Log.e("WID", "onPostExecute");
                        unlockButton();
                        setHP(hp_p, hp_t);
                        setText();
                    }
                }
            }
        }
    }

    public static class selectWBTDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.choose_wbt)
                    .setItems(R.array.webertons, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // first position is 0, so increase the value by 1 to match our
                            // Webertron types
                            // call the classes like this to prevent the need to be able to call
                            // non-static content from the static DialogFragment
                            // http://stackoverflow.com/questions/15414908/should-an-internal-dialogfragment-class-be-static-or-not
                            ((FightEngine) getActivity()).wbt_type_p = which + 1;
                            ((FightEngine) getActivity()).prepareNextFight2ndPart();
                        }
                    });
            return builder.create();
        }
    }
}
