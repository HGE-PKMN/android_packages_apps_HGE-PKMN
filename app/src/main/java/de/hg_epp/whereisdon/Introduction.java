package de.hg_epp.whereisdon;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Introduction Text to display our Story
 *
 * (c) 2015 Jan Zartmann
 * (c) 2015 Christian Oder
 */

public class Introduction extends ActionBarActivity {

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
        setContentView(R.layout.activity_introduction);
        setStartText();
    }

    private void setStartText() {
        TextView intro = (TextView) findViewById(R.id.intro_text);
        intro.setText(setRaw());
        intro.setMovementMethod(new ScrollingMovementMethod());


/*        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
                waste();
            }
        }, 50000);
*/
    }

    private String setRaw() {
        InputStream inputStream = getResources().openRawResource(R.raw.exposition);
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

/*    private void waste() {
    }
    */
}
