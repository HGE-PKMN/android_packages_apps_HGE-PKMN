package de.hg_epp.whereisdon;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    /**
     * @author Jan Zartmann and Christian Oder
     * https://developer.android.com/training/system-ui/immersive.html
     * Implementation of the Google Non-Sticky Immersive Mode
     */
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
        setContentView(R.layout.activity_main);
    }
    //method gets invoked when someone clicks a button with the onClick setting
    public void buttonOnClick(View z)
    {
        switch (z.getId()) {
            case R.id.continue_button:
                ((Button) z).setText(getString(R.string.menu_resuming));
                this.startActivity(new Intent(this, TMXTiledMapDigital.class));
                break;
            case R.id.restart_button:
                Toast.makeText(this, getString(R.string.restart_game), Toast.LENGTH_SHORT);
                break;
            case R.id.gender_change_radiobutton:
                //TMXTiledMap.onCreateResources();
                Toast.makeText(this, getString(R.string.boy_selected), Toast.LENGTH_SHORT).show();
                break;
            case R.id.gender_change_radiobutton_2:
                //TMXTiledMap.onCreateResources();
                Toast.makeText(this, getString(R.string.girl_selected), Toast.LENGTH_SHORT).show();
                break;
        }
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
}
