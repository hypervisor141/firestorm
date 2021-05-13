package com.nurverek.firestorm;

public final class FSControl{

    public static final String LOGTAG = "FIRESTORM";

    public static final int DEBUG_DISABLED = 0;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_FULL = 2;

    protected static FSView view;
    protected static FSEvents events;
    protected static FSSurface surface;

    protected static boolean destroyonpause;
    protected static boolean isalive;

    private static long GLOBAL_ID;
    private static final Object IDLOCK = new Object();

    public static void initialize(FSSurface surface, FSView view, FSRInterface threadinterface, boolean destroyonpause, int extraelementscount, int maxunchangedframes, int maxqueuedframes){
        FSControl.surface = surface;
        FSControl.events = surface.events();
        FSControl.view = view;
        FSControl.destroyonpause = destroyonpause;

        if(!isAlive()){
            GLOBAL_ID = 1000;

            FSGlobal.initialize(extraelementscount);
            FSCInput.initialize();
            FSCFrames.initialize(maxunchangedframes, maxqueuedframes);
            FSR.initialize(threadinterface);
            FSCThreads.initialize();
        }
    }

    public static long getNextID(){
        synchronized(IDLOCK){
            return GLOBAL_ID++;
        }
    }

    protected static void setAlive(boolean alive){
        FSControl.isalive = alive;
    }

    public static void setDestroyOnPause(boolean enabled){
        destroyonpause = enabled;
    }

    public static boolean getDestroyOnPause(){
        return destroyonpause;
    }

    protected static FSSurface surface(){
        return surface;
    }

    protected static FSEvents events(){
        return events;
    }

    public static boolean isAlive(){
        return isalive;
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
        FSGlobal.destroy();

        if(!destroyonpause){
            GLOBAL_ID = -1;
            isalive = false;

            surface = null;
            events = null;
            view = null;
        }
    }
}
