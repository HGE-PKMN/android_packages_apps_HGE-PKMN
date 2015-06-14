package de.hg_epp.whereisdon;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

/**
 * @author Jan Zartmann
 * @version 1.0
 */

public class ResourceManager {

    private static final ResourceManager Instance = new ResourceManager();

    public Engine engine;
    public Camera camera;
    public BaseGameActivity activity;
    public VertexBufferObjectManager vbom;

    //loadGame method loads all GameFiles together
    public void loadGame()
    {
        loadGameGraphic();
        loadGameAudio();
    }

    //loadStartScreen method loads all needed files for the start screen
    public void loadStartScreen()
    {
        loadScreen();
    }

    private void loadGameGraphic()
    {

    }

    private void loadGameAudio()
    {

    }

    private void loadScreen()
    {

    }


    //preparation method, which sets parameters vbom, activity, engine and camera. That makes it possible to use them from different classes

    public static void prepManager(VertexBufferObjectManager vbom, BaseGameActivity activity, Engine engine, Camera camera) {

        getInstance().vbom = vbom;
        getInstance().activity = activity;
        getInstance().engine = engine;
        getInstance().camera = camera;
    }

    //setter and getter
    public static ResourceManager getInstance() {
        return Instance;
    }

}
