package de.hg_epp.whereisdon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXObject;
import org.andengine.extension.tmx.TMXObjectGroup;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Based Off TMXTiledMapExample.java by
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 * <p/>
 * https://developer.android.com/training/system-ui/immersive.html
 * Implementation of the Google Non-Sticky Immersive Mode
 * <p/>
 * (c) 2015 Christian Oder
 * (c) 2015 Jan Zartmann
 */
public class TMXTiledMapDigital extends SimpleBaseGameActivity {
    // ===========================================================
    // Constants
    // ===========================================================

    public static final String PREFS_NAME = "WIDPrefs";
    private static final int PLAYER_VELOCITY = 3;

    // ===========================================================
    // Fields
    // ===========================================================

    private int mMapID;
    private BoundCamera mCamera;

    private TiledTextureRegion mPlayerTextureRegion;

    private org.andengine.extension.tmx.TMXTiledMap mTMXTiledMap;

    private Scene mScene;
    private PhysicsWorld mPhysicsWorld;

    private MediaPlayer mMusic;
    private MediaPlayer mMusicChangeMap;

    static AnimatedSprite mPlayer;
    private Body mPlayerBody;

    private Rectangle upstairs = null;
    private Rectangle downstairs = null;
    private Rectangle fight_zone[] = {};
    // private Rectangle moser_hidden = null;
    private boolean wasPaused = false;
    private String[] mMapsArray;
    float spawnX;
    float spawnY;

    private ITextureRegion mOnScreenControlBaseTextureRegion;
    private ITextureRegion mOnScreenControlKnobTextureRegion;

    private enum PlayerDirection {
        NONE,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private PlayerDirection playerDirection = PlayerDirection.NONE;
    private static final long[] ANIMATE_DURATION = new long[]{200, 200, 200};

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // Non Sticky Immersive Mode
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
    protected void onPause() {
        super.onPause();
        wasPaused = true;
        //pause that music, else it keeps on playing while minimized
        this.mMusic.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasPaused) {
            this.mMusic.start();
            this.mMusic.setLooping(true);
        }
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        int CAMERA_HEIGHT = 165;

        // set the CAMERA_WIDTH in an fitting ratio compared to the screen size.
        // this should avoid those ugly white bars across devices with different screens
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float aspectRatio = (float) displayMetrics.widthPixels / (float) displayMetrics.heightPixels;
        int CAMERA_WIDTH = Math.round(aspectRatio * CAMERA_HEIGHT);

        this.mCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        this.mCamera.setBoundsEnabled(false);

        final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
        engineOptions.getAudioOptions().setNeedsMusic(true);
        engineOptions.getAudioOptions().setNeedsSound(true);
        return engineOptions;
    }

    @Override
    public void onCreateResources() throws IOException {
        ITexture mPlayerTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), MainActivity.mChar, TextureOptions.DEFAULT);
        this.mPlayerTextureRegion = TextureRegionFactory.extractTiledFromTexture(mPlayerTexture, 3, 4);
        mPlayerTexture.load();

        ITexture mOnScreenControlBaseTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/onscreen_control_base.png", TextureOptions.BILINEAR);
        this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.extractFromTexture(mOnScreenControlBaseTexture);
        mOnScreenControlBaseTexture.load();

        ITexture mOnScreenControlKnobTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/onscreen_control_knob.png", TextureOptions.BILINEAR);
        this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.extractFromTexture(mOnScreenControlKnobTexture);
        mOnScreenControlKnobTexture.load();

        mMusicChangeMap = MediaPlayer.create(this, R.raw.downstairs);
    }


    @Override
    public Scene onCreateScene() {

        Resources r = getResources();
        mMapsArray = r.getStringArray(R.array.maps);
        mMapID = ResourceManager.getMapID();
        String mMapPath = mMapsArray[mMapID];

        this.mEngine.registerUpdateHandler(new FPSLogger());

        mScene = new Scene();

        mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, 0), false, 8, 1);
        mScene.registerUpdateHandler(mPhysicsWorld);

        try {
            final TMXLoader tmxLoader = new TMXLoader(this.getAssets(), this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getVertexBufferObjectManager(), new ITMXTilePropertiesListener() {
                @Override
                public void onTMXTileWithPropertiesCreated(final org.andengine.extension.tmx.TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
                }
            });
            this.mTMXTiledMap = tmxLoader.loadFromAsset(mMapPath);
            this.mTMXTiledMap.setOffsetCenter(0, 0);

            mScene.attachChild(this.mTMXTiledMap);

        } catch (final TMXLoadException e) {
            Debug.e(e);
        }

		/* Make the camera not exceed the bounds of the TMXEntity. */
        this.mCamera.setBounds(0, 0, this.mTMXTiledMap.getWidth(), this.mTMXTiledMap.getHeight());
        this.mCamera.setBoundsEnabled(true);

		/* Create the sprite and add it to the scene. */
        // only place game create player !
        for (final TMXObjectGroup group : mTMXTiledMap.getTMXObjectGroups()) {
            if (group.getTMXObjectGroupProperties().containsTMXProperty(
                    "spawn", "true")) {
                for (final TMXObject object : group.getTMXObjects()) {
                    spawnX = object.getX();
                    spawnY = object.getY();

                    mPlayer = new AnimatedSprite(spawnX, spawnY, mPlayerTextureRegion, getVertexBufferObjectManager());
                }
            }
        }

        mCamera.setChaseEntity(mPlayer);
        final FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0.5f);
        mPlayerBody = PhysicsFactory.createBoxBody(mPhysicsWorld, mPlayer, BodyDef.BodyType.DynamicBody, fixtureDef);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mPlayer, mPlayerBody, true, false) {

            @Override
            public void onUpdate(float pSecondsElapsed) {
                super.onUpdate(pSecondsElapsed);
                mCamera.updateChaseEntity();
            }

        });

//		mPhysicsHandler = new PhysicsHandler(mPlayerSprite);
//		mPlayerSprite.registerUpdateHandler(mPhysicsHandler);
        mScene.attachChild(mPlayer);

        		/* Velocity control (left). */
        // Amount of Pixels away from 0/0 (top left corner)
        final float edge_space = 40;
        DigitalOnScreenControl mDigitalOnScreenControl = new DigitalOnScreenControl(edge_space, edge_space, this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IOnScreenControlListener() {

            @Override
            public void onControlChange(BaseOnScreenControl pBaseOnScreenControl,
                                        float pValueX, float pValueY) {

                // Set the correct walking animation
                if (pValueX == 1) {
                    // Up
                    if (playerDirection != PlayerDirection.UP) {
                        mPlayer.animate(ANIMATE_DURATION, 3, 5, true);
                        playerDirection = PlayerDirection.UP;
                    }
                } else if (pValueX == -1) {
                    // Down
                    if (playerDirection != PlayerDirection.DOWN) {
                        mPlayer.animate(ANIMATE_DURATION, 9, 11, true);
                        playerDirection = PlayerDirection.DOWN;
                    }
                } else if (pValueY == 1) {
                    // Left
                    if (playerDirection != PlayerDirection.LEFT) {
                        mPlayer.animate(ANIMATE_DURATION, 0, 2, true);
                        playerDirection = PlayerDirection.LEFT;
                    }
                } else if (pValueY == -1) {
                    // Right
                    if (playerDirection != PlayerDirection.RIGHT) {
                        mPlayer.animate(ANIMATE_DURATION, 6, 8, true);
                        playerDirection = PlayerDirection.RIGHT;
                    }
                } else {
                    if (mPlayer.isAnimationRunning()) {
                        mPlayer.stopAnimation();
                        playerDirection = PlayerDirection.NONE;
                    }
                }
                // Set the player's velocity
                mPlayerBody.setLinearVelocity(pValueX * PLAYER_VELOCITY, pValueY * PLAYER_VELOCITY);
            }
        });

        mDigitalOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mDigitalOnScreenControl.setAlpha(0.5f);
        mDigitalOnScreenControl.getControlBase().setScale(0.5f);

        mScene.setChildScene(mDigitalOnScreenControl);

        mMusic = MediaPlayer.create(this, R.raw.background_music);
        this.mMusic.start();
        this.mMusic.setLooping(true);
        createObjects();
        updatePlayer();

        return mScene;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void startFEAsync(int tID, int mapID) {
        new FEAsyncJSONTask(tID, mapID).execute("teacher.json");
    }

    public void startFE(String t_name, String t_token, int t_level, String MapID, String TeacherID) {
        Intent startAct = new Intent(this, FightEngine.class);

        startAct.setAction(Intent.ACTION_SEND);
        startAct.putExtra(Intent.EXTRA_SUBJECT, "CreateFight");
        startAct.putExtra(Intent.EXTRA_TITLE, t_name);
        startAct.setType("text/plain");
        startAct.putExtra(Intent.EXTRA_TEXT, Integer.toString(t_level));
        startAct.putExtra(Intent.EXTRA_UID, t_token);
        startAct.putExtra(Intent.EXTRA_SHORTCUT_NAME, MapID);
        startAct.putExtra(Intent.EXTRA_REFERRER_NAME, TeacherID);
        finish();
        this.startActivity(startAct);
    }

    public void loadMap(int map) {
        Intent startAct = new Intent(this, TMXTiledMapDigital.class);
        ResourceManager.setMapID(map);
        finish();
        this.startActivity(startAct);
    }

    public void showWinAnimation(){
        //Intent startAct = new Intent(this, DonWin.class);
        finish();
        //this.startActivity(startAct);
    }

    static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

    private void createObjects() {
        // Loop through the object groups
        Log.e("WID", "0");
        for (final TMXObjectGroup group : this.mTMXTiledMap.getTMXObjectGroups()) {

            // Check for wall objects
            Log.e("WID", "1");
            if (group.getTMXObjectGroupProperties().containsTMXProperty("wall", "true")) {
                // This is our "wall" layer. Create the boxes from it
                Log.e("WID", "2");
                for (final TMXObject object : group.getTMXObjects()) {
                    Log.e("WID", "3");
                    final Rectangle rect = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight(), getVertexBufferObjectManager());
                    final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 1f);
                    rect.setOffsetCenter(0, 0);
                    rect.setVisible(false);
                    PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyDef.BodyType.StaticBody, wallFixtureDef);

                    mScene.attachChild(rect);
                }
            }

            if (group.getTMXObjectGroupProperties().containsTMXProperty("stairs_up", "true")) {
                // This is our "upstair" layer. Create the boxes from it
                for (final TMXObject object : group.getTMXObjects()) {
                    upstairs = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight(), getVertexBufferObjectManager());
                    upstairs.setOffsetCenter(0, 0);
                    upstairs.setVisible(false);
                    mScene.attachChild(upstairs);
                }
            }

            if (group.getTMXObjectGroupProperties().containsTMXProperty("stairs_down", "true")) {
                // This is our "downstair" layer. Create the boxes from it
                for (final TMXObject object : group.getTMXObjects()) {
                    downstairs = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight(), getVertexBufferObjectManager());
                    downstairs.setOffsetCenter(0, 0);
                    downstairs.setVisible(false);
                    mScene.attachChild(downstairs);
                }
            }

            if (group.getTMXObjectGroupProperties().containsTMXProperty("teacher_fight", "true")) {
                // This is our "teacher fight" layer. Create the boxes from it
                int fight_zone_id = 0;
                for (final TMXObject object : group.getTMXObjects()) {
                    //go fight with them

                    Rectangle rect = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight(), getVertexBufferObjectManager());
                    rect.setOffsetCenter(0, 0);
                    rect.setVisible(false);
                    fight_zone = append(fight_zone, rect);

                    mScene.attachChild(fight_zone[fight_zone_id]);
                    Log.e("WID", "FZI" + fight_zone_id);
                    fight_zone_id++;
                }
            }

/*            if (group.getTMXObjectGroupProperties().containsTMXProperty("moser_hidden", "true")) {
                // This is our "Mr. Moser" layer. Create the boxes from it
                for (final TMXObject object : group.getTMXObjects()) {
                    //go upstairs if all teachers have been defeated
                    moser_hidden = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight(), getVertexBufferObjectManager());
                }
            }*/
        }
    }

    public void updatePlayer() {
        mScene.registerUpdateHandler(new IUpdateHandler() {
                                         @Override
                                         public void reset() {/* Not used */
                                         }

                                         @Override
                                         public void onUpdate(final float pSecondsElapsed) {
                                             if (upstairs != null) {
                                                 if (upstairs.collidesWith(mPlayer)) {
                                                     SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                                     SharedPreferences.Editor editor = settings.edit();
                                                     int maxID = settings.getInt("maxMapID", 0);
                                                     boolean t1 = settings.getBoolean("t1", false);
                                                     boolean t2 = settings.getBoolean("t2", false);
                                                     boolean t3 = settings.getBoolean("t3", false);
                                                     boolean t4 = settings.getBoolean("t4", false);
                                                     boolean t5 = settings.getBoolean("t5", false);
                                                     boolean t6 = settings.getBoolean("t6", false);
                                                     if (mMapID < maxID) {
                                                         loadMap(mMapID + 1);
                                                         Log.d("WID", "going upstairs!");
                                                     } else {
                                                         if (t1 && t2 && t3 && t4 && t5 && t6) {
                                                             if(maxID + 1 == mMapsArray.length){
                                                                 Log.d("WID", "YOU WON!!");
                                                                 showWinAnimation();
                                                             }else {
                                                                 editor.putInt("maxMapID", mMapID + 1);
                                                                 editor.putBoolean("t1", false);
                                                                 editor.putBoolean("t2", false);
                                                                 editor.putBoolean("t3", false);
                                                                 editor.putBoolean("t4", false);
                                                                 editor.putBoolean("t5", false);
                                                                 editor.putBoolean("t6", false);
                                                                 editor.apply();
                                                                 loadMap(mMapID + 1);
                                                                 Log.d("WID", "going upstairs!");
                                                             }
                                                         }
                                                     }
                                                 }
                                             }

                                             if (downstairs != null) {
                                                 if (downstairs.collidesWith(mPlayer)) {
                                                     mMusicChangeMap.start();
                                                     loadMap(mMapID - 1);
                                                     Log.d("WID", "going downstairs!");
                                                 }
                                             }

                                             if (fight_zone != null) {
                                                 for (int i = 0; i < fight_zone.length; i++)
                                                     if (fight_zone[i] != null) {
                                                         if (fight_zone[i].collidesWith(mPlayer)) {
                                                             startFEAsync(mMapID, i + 1);
                                                         }
                                                     }
                                             }
/*                        if (moser_hidden != null) {
                            if (moser_hidden.collidesWith(mPlayer)) {

                            }
                        }*/
                                         }// end inner class method
                                     }
        );
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * AsyncTask to read the Teacher JSON Table for our FightEngine.
     * (c) 2015 Christian Oder
     */
    public class FEAsyncJSONTask extends AsyncTask<String, String, String> {

        private String mMapID;
        private String mTeacherID;
        private String mTeacherName;
        private String mTeacherToken;
        private int mTeacherWonFights;

        public FEAsyncJSONTask(int mID, int tID) {
            mMapID = Integer.toString(mID);
            mTeacherID = Integer.toString(tID);
        }

        @Override
        protected String doInBackground(String... afile) {
            String mFile = afile[0];
            try {
                StringBuilder buf = new StringBuilder();
                InputStream json = getAssets().open(mFile);
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(json, "UTF-8"));
                String str;

                while ((str = in.readLine()) != null) {
                    buf.append(str);
                }
                in.close();

                JSONObject obj = new JSONObject(buf.toString().trim());
                JSONObject obj2 = obj.getJSONObject(mMapID);
                JSONObject obj3 = obj2.getJSONObject(mTeacherID);
                mTeacherName = obj3.getString("teacher_name");
                mTeacherToken = obj3.getString("teacher_token");
                mTeacherWonFights = obj3.getInt("teacher_won");

                return "true";

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }


            return "null";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("true")) {
                startFE(mTeacherName, mTeacherToken, mTeacherWonFights, mMapID, mTeacherID);
            }
        }
    }
}