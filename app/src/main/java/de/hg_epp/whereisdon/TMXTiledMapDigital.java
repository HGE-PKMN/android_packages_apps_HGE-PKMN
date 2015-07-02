package de.hg_epp.whereisdon;

import android.content.Intent;
import android.opengl.GLES20;
import android.util.Log;
import android.view.View;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
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
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Based Off TMXTiledMapExample.java by
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 * <p/>
 * modified by Christian Oder for steering of the Sprite and FullScreen Mode
 * using Google Non Sticky Immersive Mode
 *
 * @author Christian Oder
 * @author Jan Zartmann
 */
public class TMXTiledMapDigital extends SimpleBaseGameActivity {

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

    // ===========================================================
    // Constants
    // ===========================================================

    private static final int CAMERA_WIDTH = 240;
    private static final int CAMERA_HEIGHT = 135;
    private static final float ELASTICITY = 0;
    private static final float FRICTION = 1f;
    private static final int PLAYER_VELOCITY = 3;
    private boolean wasPaused = false;

    // ===========================================================
    // Fields
    // ===========================================================

    private BoundCamera mCamera;

    private ITexture mPlayerTexture;
    private TiledTextureRegion mPlayerTextureRegion;

    private org.andengine.extension.tmx.TMXTiledMap mTMXTiledMap;

    private Scene mScene;
    private PhysicsWorld mPhysicsWorld;

    private Music mMusic;

    static AnimatedSprite mPlayer;
    private Body mPlayerBody;

    private ITexture mOnScreenControlBaseTexture;
    private ITextureRegion mOnScreenControlBaseTextureRegion;
    private ITexture mOnScreenControlKnobTexture;
    private ITextureRegion mOnScreenControlKnobTextureRegion;

    private DigitalOnScreenControl mDigitalOnScreenControl;

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
        if (wasPaused)
            this.mMusic.play();
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.mCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        this.mCamera.setBoundsEnabled(false);

        final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
        engineOptions.getAudioOptions().setNeedsMusic(true);
        engineOptions.getAudioOptions().setNeedsSound(true);
        return engineOptions;
    }

    @Override
    public void onCreateResources() throws IOException {
        this.mPlayerTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), MainActivity.mChar, TextureOptions.DEFAULT);
        this.mPlayerTextureRegion = TextureRegionFactory.extractTiledFromTexture(this.mPlayerTexture, 3, 4);
        this.mPlayerTexture.load();

        this.mOnScreenControlBaseTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/onscreen_control_base.png", TextureOptions.BILINEAR);
        this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.extractFromTexture(this.mOnScreenControlBaseTexture);
        this.mOnScreenControlBaseTexture.load();

        this.mOnScreenControlKnobTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/onscreen_control_knob.png", TextureOptions.BILINEAR);
        this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.extractFromTexture(this.mOnScreenControlKnobTexture);
        this.mOnScreenControlKnobTexture.load();

        try {
            mMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "mfx/background_music.ogg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Scene onCreateScene() {


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
            this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/mapchris.tmx");
            this.mTMXTiledMap.setOffsetCenter(0, 0);

            mScene.attachChild(this.mTMXTiledMap);

        } catch (final TMXLoadException e) {
            Debug.e(e);
        }

		/* Make the camera not exceed the bounds of the TMXEntity. */
        TMXLayer tmxLayer = mTMXTiledMap.getTMXLayers().get(0);
        this.mCamera.setBounds(0, 0, this.mTMXTiledMap.getWidth(), this.mTMXTiledMap.getHeight());
        this.mCamera.setBoundsEnabled(true);
        // addBounds(tmxLayer.getWidth(), tmxLayer.getHeight());

        final float centerX = CAMERA_WIDTH / 2;
        final float centerY = CAMERA_HEIGHT / 2;

		/* Create the sprite and add it to the scene. */
        mPlayer = new AnimatedSprite(centerX, centerY, mPlayerTextureRegion, getVertexBufferObjectManager());
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
        this.mDigitalOnScreenControl = new DigitalOnScreenControl(edge_space, edge_space, this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IOnScreenControlListener() {

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

        this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        this.mDigitalOnScreenControl.setAlpha(0.5f);
        this.mDigitalOnScreenControl.getControlBase().setScale(0.5f);

        mScene.setChildScene(this.mDigitalOnScreenControl);

        this.mMusic.play();
        this.mMusic.setLooping(true);
        createUnwalkableObjects(this.mTMXTiledMap);
        Log.e("WID", "4");

        return mScene;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void startFE(String t_name, String t_token, int t_level) {
        Intent startAct = new Intent(this, FightEngine.class);

        startAct.setAction(Intent.ACTION_SEND);
        startAct.putExtra(Intent.EXTRA_SUBJECT, "CreateFight");
        startAct.putExtra(Intent.EXTRA_TITLE, t_name);
        startAct.setType("text/plain");
        startAct.putExtra(Intent.EXTRA_TEXT, Integer.toString(t_level));
        startAct.putExtra(Intent.EXTRA_UID, t_token);
        this.startActivity(startAct);
    }

    private void addBounds(float width, float height) {
        final IEntity bottom = new Rectangle(0, height - 2, width, 2, getVertexBufferObjectManager());
        bottom.setVisible(false);
        final IEntity top = new Rectangle(0, 0, width, 2, getVertexBufferObjectManager());
        top.setVisible(false);
        final IEntity left = new Rectangle(0, 0, 2, height, getVertexBufferObjectManager());
        left.setVisible(false);
        final IEntity right = new Rectangle(width - 2, 0, 2, height, getVertexBufferObjectManager());
        right.setVisible(false);

        final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 1f);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, bottom, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, top, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyDef.BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyDef.BodyType.StaticBody, wallFixtureDef);

        this.mScene.attachChild(bottom);
        this.mScene.attachChild(top);
        this.mScene.attachChild(left);
        this.mScene.attachChild(right);
    }

    private void createUnwalkableObjects(TMXTiledMap map) {
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
                    rect.setColor(1, 0, 0, 0.25f);
                    PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyDef.BodyType.StaticBody, wallFixtureDef);

                    mScene.attachChild(rect);
                }
            }


        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}