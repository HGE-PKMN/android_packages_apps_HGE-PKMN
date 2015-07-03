package de.hg_epp.whereisdon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public static String mChar;
    public static final String PREFS_NAME = "WIDPrefs";

    /**
     * MainMenu for our Game. It manages the main stuff and
     * (c) 2015 Jan Zartmann
     * (c) 2015 Christian Oder
     * <p/>
     * https://developer.android.com/training/system-ui/immersive.html
     * Implementation of the Google Non-Sticky Immersive Mode
     */
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //method gets invoked when someone clicks a button with the onClick setting
    public void buttonOnClick(View z) {
        switch (z.getId()) {
            case R.id.continue_button:
                SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                if(settings.getBoolean("intro_run", true)){
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("intro_run", false);
                    editor.apply();
                    this.startActivity(new Intent(this, Introduction.class));
                }else {
                    this.startActivity(new Intent(this, TMXTiledMapDigital.class));
                }
                break;
            case R.id.restart_button:
                reallyResetGame();
                break;
            case R.id.gender_changer_radio_boy:
                Toast.makeText(this, getString(R.string.boy_selected), Toast.LENGTH_SHORT).show();
                mChar = "gfx/trainer.png";
                storeButtonState();

                break;
            case R.id.gender_changer_radio_girl:
                Toast.makeText(this, getString(R.string.girl_selected), Toast.LENGTH_SHORT).show();
                mChar = "gfx/player.png";
                storeButtonState();
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean radio_boy = settings.getBoolean("radio_boy", true);
        boolean radio_girl = settings.getBoolean("radio_girl", false);
/*        if (radio_boy) {*/
        ((RadioButton) findViewById(R.id.gender_changer_radio_boy)).setChecked(radio_boy);
/*        }else if(radio_girl){*/
        ((RadioButton) findViewById(R.id.gender_changer_radio_girl)).setChecked(radio_girl);
/*        }*/
        mChar = settings.getString("mChar_dir", "gfx/trainer.png");
    }

    private void storeButtonState() {
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("radio_boy", ((RadioButton) findViewById(R.id.gender_changer_radio_boy)).isChecked());
        editor.putBoolean("radio_girl", ((RadioButton) findViewById(R.id.gender_changer_radio_girl)).isChecked());
        editor.putString("mChar_dir", mChar);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetGame(){
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("intro_run", true);
        editor.putString("mChar_dir", "gfx/trainer.png");
        editor.apply();
    }

    public void reallyResetGame() {
        DialogFragment DialogFragment = new resetGameDialog();
        DialogFragment.show(getFragmentManager(), "reset");
    }

    public class resetGameDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.resetGame)
                    .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            resetGame();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
