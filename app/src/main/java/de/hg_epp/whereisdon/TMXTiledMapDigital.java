package de.hg_epp.whereisdon;

import android.hardware.SensorManager;
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
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
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
import org.andengine.util.Constants;
import org.andengine.util.debug.Debug;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Based Off TMXTiledMapExample.java by
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * modified by Christian Oder for steering of the Sprite and FullScreen Mode
 * using Google Non Sticky Immersive Mode
 * @author Christian Oder
 */
public class TMXTiledMapDigital extends SimpleBaseGameActivity  {

    // Non Sticky Immersive Mode
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

    // ===========================================================
    // Constants
    // ===========================================================

    private static final int CAMERA_WIDTH = 240;
    private static final int CAMERA_HEIGHT = 135;

    // ===========================================================
    // Fields
    // ===========================================================

    private BoundCamera mCamera;

    private ITexture mPlayerTexture;
    private TiledTextureRegion mPlayerTextureRegion;

    private org.andengine.extension.tmx.TMXTiledMap mTMXTiledMap;

    private Scene mScene;
    private PhysicsWorld mPhysicsWorld;
    private static final float ELASTICITY = 0f;
    private static final float FRICTION = 0.5f;
    private ArrayList<Body> walls = new ArrayList<>();

    private Music mMusic;
    private Music mMainMusic;

    static AnimatedSprite mPlayer;

    private ITexture mOnScreenControlBaseTexture;
    private ITextureRegion mOnScreenControlBaseTextureRegion;
    private ITexture mOnScreenControlKnobTexture;
    private ITextureRegion mOnScreenControlKnobTextureRegion;

    private DigitalOnScreenControl mDigitalOnScreenControl;

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

        //load music
        try
        {
            mMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this,"mfx/background_music.ogg");
            mMainMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this,"mfx/menu_close1.ogg");
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public Scene onCreateScene() {

        
        this.mEngine.registerUpdateHandler(new FPSLogger());

        mScene = new Scene();
        mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        mScene.registerUpdateHandler(mPhysicsWorld);

        try {
            final TMXLoader tmxLoader = new TMXLoader(this.getAssets(), this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getVertexBufferObjectManager(), new ITMXTilePropertiesListener() {
                @Override
                public void onTMXTileWithPropertiesCreated(final org.andengine.extension.tmx.TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
                }
            });
            this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/mapchris.tmx");
            this.mTMXTiledMap.setOffsetCenter(0, 0);

            createUnwalkableObjects(this.mTMXTiledMap);
            mScene.attachChild(this.mTMXTiledMap);

        } catch (final TMXLoadException e) {
            Debug.e(e);
        }

		/* Make the camera not exceed the bounds of the TMXEntity. */
        this.mCamera.setBoundsEnabled(false);
        this.mCamera.setBounds(0, 0, this.mTMXTiledMap.getWidth(), this.mTMXTiledMap.getHeight());
        this.mCamera.setBoundsEnabled(true);

        final float centerX = CAMERA_WIDTH / 2;
        final float centerY = CAMERA_HEIGHT / 2;

		/* Create the sprite and add it to the scene. */
        mPlayer = new AnimatedSprite(centerX, centerY, this.mPlayerTextureRegion, this.getVertexBufferObjectManager());
        mPlayer.setOffsetCenterY(0);
        this.mCamera.setChaseEntity(mPlayer);
        final PhysicsHandler physicsHandler = new PhysicsHandler(mPlayer);
        mPlayer.registerUpdateHandler(physicsHandler);

        		/* Velocity control (left). */
        // Amount of Pixels away from 0/0 (top left corner)
        final float edge_space = 40;
        this.mDigitalOnScreenControl = new DigitalOnScreenControl(edge_space, edge_space, this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IOnScreenControlListener() {
            float lastpValueX;
            float lastpValueY;
            @Override
            public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
                if(pValueX != lastpValueX || pValueY != lastpValueY) {
                    lastpValueX = pValueX;
                    lastpValueY = pValueY;
                    if (pValueX == 1) {
                        mPlayer.animate(new long[]{200, 200, 200}, 3, 5, true);
                    } else if (pValueX == -1) {
                        mPlayer.animate(new long[]{200, 200, 200}, 9, 11, true);
                    } else if (pValueY == 1) {
                        mPlayer.animate(new long[]{200, 200, 200}, 0, 2, true);
                    } else if (pValueY == -1) {
                        mPlayer.animate(new long[]{200, 200, 200}, 6, 8, true);
                    } else if (pValueX == 0 && pValueY == 0) {
                        mPlayer.stopAnimation();
                    }
                    physicsHandler.setVelocity(pValueX * 100, pValueY * 100);
                }
            }
        });

        this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        this.mDigitalOnScreenControl.setAlpha(0.5f);
        this.mDigitalOnScreenControl.getControlBase().setScale(0.5f);

        mScene.setChildScene(this.mDigitalOnScreenControl);


		/* Now we are going to create a rectangle that will  always highlight the tile below the feet of the pEntity. */
        final Rectangle currentTileRectangle = new Rectangle(0, 0, this.mTMXTiledMap.getTileWidth(), this.mTMXTiledMap.getTileHeight(), this.getVertexBufferObjectManager());
		/* Set the OffsetCenter to 0/0, so that it aligns with the TMXTiles. */
        //currentTileRectangle.setOffsetCenter(0, 0);
        //currentTileRectangle.setColor(1, 0, 0, 0.25f);
        //scene.attachChild(currentTileRectangle);

		/* The layer for the player to walk on. */
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);

        mScene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void reset() {
            }

            @Override
            public void onUpdate(final float pSecondsElapsed) {
				/* Get the scene-coordinates of the players feet. */
                final float[] playerFootCordinates = mPlayer.convertLocalCoordinatesToSceneCoordinates(16, 1);

				/* Get the tile the feet of the player are currently waking on. */
                final TMXTile tmxTile = tmxLayer.getTMXTileAt(playerFootCordinates[Constants.VERTEX_INDEX_X], playerFootCordinates[Constants.VERTEX_INDEX_Y]);
                if (tmxTile != null) {
                    // tmxTile.setTextureRegion(null); <-- Eraser-style removing of tiles =D
                    currentTileRectangle.setPosition(tmxLayer.getTileX(tmxTile.getTileColumn()), tmxLayer.getTileY(tmxTile.getTileRow()));
                }
            }
        });
        mScene.attachChild(mPlayer);
        this.mMusic.play();
        this.mMusic.setLooping(true);

        return mScene;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void createUnwalkableObjects(TMXTiledMap map){
// Loop through the object groups
        for (final TMXObjectGroup group : mTMXTiledMap.getTMXObjectGroups()) {
            if (group.getTMXObjectGroupProperties().containsTMXProperty("wall",
                    "true")) {
                // This is our "wall" layer. Create the physical boxes from it
                for (final TMXObject object : group.getTMXObjects()) {
                    // Create the rectangle
                    final Rectangle rect = new Rectangle(object.getX(),
                            object.getY(), object.getWidth(),
                            object.getHeight(), getVertexBufferObjectManager());
                    //make the body
                    final FixtureDef boxFixtureDef = PhysicsFactory
                            .createFixtureDef(0, ELASTICITY, FRICTION);
                    //connect the body to the physics engine.
                    Body tempbody = PhysicsFactory.createBoxBody(mPhysicsWorld, rect,
                            BodyDef.BodyType.StaticBody, boxFixtureDef);

                    // make it invisible
                    rect.setVisible(false);
                    //add it to the scene
                    walls.add(tempbody);
                    //rects.add(rect);
                    mScene.attachChild(rect);
                }
            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}