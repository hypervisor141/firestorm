package com.nurverek.firestorm;

import vanguard.VLThreadManager;

public class FSThreadManager{

    private static VLThreadManager manager;

    protected static void initialize(){
        manager = new VLThreadManager(8);
    }

    public static VLThreadManager manager(){
        return manager;
    }

    public static void destroy(){
        manager.destroy();
    }

}
