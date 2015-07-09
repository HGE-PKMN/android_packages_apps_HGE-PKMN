package de.hg_epp.whereisdon;

/**
 * Resources Class for our Game. It manages the Map Ressources
 * (c) 2015 Jan Zartmann
 * (c) 2015 Christian Oder
 *
 * @version 1.0
 */

public class ResourceManager {

    private static int mMapID;

    // store Map ID for persistence across classes
    public static void setMapID(int id){
        mMapID = id;
    }

    // return Map ID for persistence across classes
    public static int getMapID(){
        return mMapID;
    }
}
