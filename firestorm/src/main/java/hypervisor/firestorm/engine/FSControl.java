package hypervisor.firestorm.engine;

import android.content.Context;

public final class FSControl{

    public static final String LOGTAG = "FIRESTORM";

    public static final int DEBUG_DISABLED = 0;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_FULL = 2;

    protected static FSView view;
    protected static FSEvents events;
    protected static FSSurface surface;
    protected static Context appcontext;

    protected static boolean destroyonpause;
    protected static boolean isalive;

    private static long GLOBAL_ID;
    private static final Object IDLOCK = new Object();

    public static void initialize(Context appcontext, FSSurface surface, FSView view, FSRInterface threadinterface, FSGlobal global, FSElements.CustomElements customelements, boolean destroyonpause, int maxunchangedframes, int maxqueuedframes){
        FSControl.appcontext = appcontext;
        FSControl.surface = surface;
        FSControl.events = surface.events();
        FSControl.view = view;
        FSControl.destroyonpause = destroyonpause;

        if(!isAlive()){
            GLOBAL_ID = 1000;

            FSElements.initialize(customelements);
            FSCInput.initialize();
            FSCFrames.initialize(maxunchangedframes, maxqueuedframes);
            FSR.initialize(threadinterface, global);
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

    public static FSSurface surface(){
        return surface;
    }

    public static Context surfaceContext(){
        return surface.getContext();
    }

    public static Context appContext(){
        return appcontext;
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
        FSElements.destroy();

        if(!destroyonpause){
            GLOBAL_ID = -1;
            isalive = false;

            appcontext = null;
            surface = null;
            events = null;
            view = null;
        }
    }
}
