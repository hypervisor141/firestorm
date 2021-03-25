package com.nurverek.firestorm;

import android.app.Activity;
import android.content.Context;

import vanguard.VLDebug;

public final class FSControl{

    public static final String LOGTAG = "FIRESTORM";
    public static final int DEBUG_DISABLED = 0;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_FULL = 2;

    protected static FSSurface surface;
    protected static Activity activity;
    protected static FSView view;

    protected static boolean keepalive;
    protected static boolean isalive;

    public static FSSurface initialize(Activity act, FSSurface surface, FSView view, FSRInterface threadinterface, boolean keepalive){
        VLDebug.tag(LOGTAG);

        FSControl.activity = act;
        FSControl.surface = surface;
        FSControl.view = view;
        FSControl.keepalive = keepalive;

        FSCInput.initialize();
        FSCFrames.initialize();
        FSR.initialize(threadinterface);
        FSCThreadManager.initialize();

        return FSControl.surface;
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

    public static Activity getActivity(){
        return activity;
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
        FSCThreadManager.destroy();

        if(!keepalive){
            FSControl.isalive = false;

            activity = null;
            view = null;
            surface = null;
        }
    }
}
