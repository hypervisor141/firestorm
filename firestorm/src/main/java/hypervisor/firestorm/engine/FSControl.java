package hypervisor.firestorm.engine;

import android.content.Context;

public final class FSControl{

    public static final String LOGTAG = "FIRESTORM";
    public static final Object ACTIVITY_ACCESS_LOCK = new Object();

    public static final int DEBUG_DISABLED = 0;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_VERBOSE = 2;

    protected static FSView view;
    protected static FSEvents events;
    protected static FSSurface surface;
    protected static Context appcontext;
    protected static FSActivity activity;

    protected static boolean destroyonpause;
    protected static boolean isalive;

    private static long GLOBAL_UID;
    private static final Object UIDLOCK = new Object();

    protected static void setActivity(FSActivity activity){
        synchronized(ACTIVITY_ACCESS_LOCK){
            FSControl.activity = activity;

            if(activity != null){
                appcontext = activity.getApplicationContext();
            }
        }
    }

    public static void initialize(FSSurface surface, FSView view, FSRInterface threadinterface, FSGlobal global, FSElements.CustomElements customelements, boolean destroyonpause, int maxunchangedframes, int maxqueuedframes){
        FSControl.surface = surface;
        FSControl.events = surface.events();
        FSControl.view = view;
        FSControl.destroyonpause = destroyonpause;

        if(!isAlive()){
            GLOBAL_UID = 1000;

            FSCDimensions.initialize(activity);
            FSElements.initialize(customelements);
            FSCFrames.initialize(maxunchangedframes, maxqueuedframes);
            FSR.initialize(threadinterface);
            FSGlobal.initialize(global);
        }
    }

    public static long generateUID(){
        synchronized(UIDLOCK){
            return GLOBAL_UID++;
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

    public static FSActivity activity(){
        synchronized(ACTIVITY_ACCESS_LOCK){
            return activity;
        }
    }

    public static FSSurface surface(){
        return surface;
    }

    public static Context surfaceContext(){
        return surface.getContext();
    }

    public static Context appContext(){
        synchronized(ACTIVITY_ACCESS_LOCK){
            return appcontext;
        }
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
        setActivity(null);

        FSR.destroy(destroyonpause);
        FSGlobal.destroy(destroyonpause);
        FSCDimensions.destroy(destroyonpause);
        FSCEGL.destroy(destroyonpause);
        FSCFrames.destroy(destroyonpause);
        FSElements.destroy(destroyonpause);

        if(destroyonpause){
            GLOBAL_UID = -1;
            isalive = false;

            appcontext = null;
            surface = null;
            events = null;
            view = null;
        }
    }
}
