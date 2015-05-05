package de.hg_epp.whereisdon;

import org.andengine.extension.tmx.*;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.opengl.texture.TextureOptions;

/**
 * Created by TheGhostof on 5/05/2015.
 */
//public class MapEngine {
//    TMXTiledMap mapa;
//    TMXLayer layer;
//
//    private void createBackground()
//
//    {
//        try {
//            final TMXLoader loader = new TMXLoader(activity.getAssets(), engine.getTextureManager(), TextureOptions.NEAREST, engine.getVertexBufferObjectManager(),new TMXLoader.ITMXTilePropertiesListener(){
//
//                @Override
//                public void onTMXTileWithPropertiesCreated(
//                        TMXTiledMap pTMXTiledMap, TMXLayer pTMXLayer,
//                        TMXTile pTMXTile,
//                        TMXProperties pTMXTileProperties) {
//// TODO Auto-generated method stub
//                }
//            });
//            mapa = loader.loadFromAsset("tmx/tutmap.tmx");
//
//        } catch (TMXLoadException e) {
//// TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        createUnwalkableObjects(mapa);
//        layer = mapa.getTMXLayers().get(0);
//        attachChild(layer);
//
//    }
//}
