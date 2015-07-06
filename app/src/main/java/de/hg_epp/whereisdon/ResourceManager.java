package de.hg_epp.whereisdon;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

/**
 * Resources Class for our Game. It manages the Map Ressources
 * (c) 2015 Jan Zartmann
 * (c) 2015 Christian Oder
 *
 * @version 1.0
 */

public class ResourceManager {

    private static int mMapID;

    public static void setMapID(int id){
        mMapID = id;
    }

    public static int getMapID(){
        return mMapID;
    }
}
