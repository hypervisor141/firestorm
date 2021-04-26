package com.nurverek.firestorm;

import android.content.Context;

public final class FSControl{

    public static final String LOGTAG = "FIRESTORM";

    public static final int DEBUG_DISABLED = 0;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_FULL = 2;

    protected static FSSurface surface;
    protected static FSView view;

    protected static boolean keepalive;
    protected static boolean isalive;

    public static FSSurface initialize(FSSurface surface, FSView view, FSRInterface threadinterface, boolean keepalive, int maxunchangedframes, int maxqueuedframes){
        FSControl.surface = surface;
        FSControl.view = view;
        FSControl.keepalive = keepalive;

        if(!isAlive()){
            FSCInput.initialize();
            FSCFrames.initialize(maxunchangedframes, maxqueuedframes);
            FSR.initialize(threadinterface);
            FSCThreads.initialize();
        }

        return surface;
    }

    protected static void setAlive(boolean alive){
        FSControl.isalive = alive;
    }

    public static void setKeepAlive(boolean enabled){
        keepalive = enabled;
    }

    public static boolean getKeepAlive(){
        return keepalive;
    }

    public static boolean isAlive(){
        return isalive;
    }

    public static Context getContext(){
        return surface.getContext();
    }

    public static FSSurface getSurface(){
        return surface;
    }

    public static FSView getView(){
        return view;
    }

    protected static void destroy(){
        FSR.destroy();
        FSCDimensions.destroy();
        FSCEGL.destroy();
        FSCInput.destroy();
        FSCFrames.destroy();

        if(!keepalive){
            isalive = false;

            view = null;
            surface = null;
        }
    }
}
