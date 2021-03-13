package com.nurverek.firestorm;

import android.app.Activity;
import android.content.Context;

import vanguard.VLDebug;

public final class FSControl{

    public static final String LOGTAG = "FIRESTORM";
    public static boolean DEBUG_GLOBALLY = true;
    public static final int DEBUG_DISABLED = 0;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_FULL = 2;

    protected static FSSurface surface;
    protected static Activity activity;
    protected static FSView viewconfig;

    protected static boolean keepalive;
    protected static float[] clearcolor;

    public static FSSurface initialize(Activity act, FSSurface surface, FSRInterface threadinterface){
        VLDebug.tag(LOGTAG);

        keepalive = false;

        clearcolor = new float[4];
        viewconfig = new FSView();

        FSControl.surface = surface;
        FSControl.activity = act;

        FSInput.initialize();
        FSRFrames.initialize();
        FSR.initialize(threadinterface);
        FSVThreadManager.initialize();

        return FSControl.surface;
    }

    public static void setKeepAlive(boolean enabled){
        keepalive = enabled;
    }

    public static void setClearColor(float r, float g, float b, float a){
        clearcolor[0] = r;
        clearcolor[1] = g;
        clearcolor[2] = b;
        clearcolor[3] = a;
    }

    public static boolean getKeepAlive(){
        return keepalive;
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

    public static FSView getViewConfig(){
        return viewconfig;
    }

    public static float[] getClearColor(){
        return clearcolor;
    }

    protected static void destroy(){
        FSR.destroy();
        FSDimensions.destroy();
        FSEGL.destroy();
        FSInput.destroy();
        FSRFrames.destroy();
        FSVThreadManager.destroy();

        if(!keepalive){
            activity = null;
            viewconfig = null;
            surface = null;
            clearcolor = null;
        }
    }
}
