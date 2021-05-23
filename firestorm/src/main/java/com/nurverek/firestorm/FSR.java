package com.nurverek.firestorm;

import android.content.Context;
import android.view.Choreographer;

import vanguard.VLListType;
import vanguard.VLThreadTaskType;

public class FSR{

    public static final FSRInterface DEFAULT_INTERFACE = new FSRInterface(){

        @Override
        public FSRThread create(){
            return new FSRThread();
        }
    };
    private static final Choreographer.FrameCallback CHOREOGRAPHER_CALLBACK = new Choreographer.FrameCallback(){

        @Override
        public void doFrame(long frameTimeNanos){
            post(new FSRThread.TaskSignalFrameDraw());
        }
    };

    private static FSRInterface threadinterface;
    private static FSGlobal global;
    private static Choreographer choreographer;
    private static volatile FSRThread renderthread;

    private static VLListType<FSRTask> tasks;
    private static VLListType<FSRTask> taskcache;

    protected static boolean isInitialized;

    public static int CURRENT_PASS_INDEX;
    public static int CURRENT_PASS_ENTRY_INDEX;

    protected static void initialize(FSRInterface threadsrc, FSGlobal global){
        FSR.threadinterface = threadsrc;
        FSR.global = global;

        choreographer = Choreographer.getInstance();

        tasks = new VLListType<>(10, 100);
        taskcache = new VLListType<>(10, 100);

        isInitialized = true;

        CURRENT_PASS_INDEX = 0;
        CURRENT_PASS_ENTRY_INDEX = 0;
    }

    public static void setFSRThreadInterface(FSRInterface threadsrc){
        threadinterface = threadsrc;
    }

    protected static void requestStart(){
        if(renderthread != null){
            renderthread.unlock();

        }else{
            renderthread = threadinterface.create();
            renderthread.setDaemon(true);
            renderthread.setPriority(8);
            renderthread.setName("FSR");
            renderthread.requestStart();
        }
    }

    protected static void requestFrame(){
        if(renderthread != null && renderthread.running()){
            choreographer.postFrameCallback(CHOREOGRAPHER_CALLBACK);
        }
    }

    protected static FSRThread post(VLThreadTaskType task){
        if(renderthread != null){
            renderthread.post(task);
        }

        return renderthread;
    }

    protected static void surfaceCreated(FSSurface surface, Context context, boolean continuing){
        FSEvents events = FSControl.events();

        events.GLPreCreated(surface, context, continuing);
        events.GLPostCreated(surface, context, continuing);

        global.initialize(context);

        if(continuing){
            FSR.notifyResumed();
        }
    }

    protected static void surfaceChanged(FSSurface surface, Context context, int format, int width, int height){
        FSEvents events = FSControl.events();

        events.GLPreChange(surface, context, format, width, height);
        events.GLPostChange(surface, context, format, width, height);

        requestFrame();
    }

    protected static void drawFrame(){
        FSCFrames.timeFrameStarted();

        FSEvents events = FSControl.events();
        events.GLPreDraw();

        VLListType<FSRPass> passes = global.passes;

        int size = passes.size();

        for(int i = 0; i < size; i++){
            CURRENT_PASS_INDEX = i;
            CURRENT_PASS_ENTRY_INDEX = -1;

            passes.get(i).draw();
        }

        synchronized(tasks){
            taskcache.add(tasks);
            tasks.clear();
        }

        size = taskcache.size();

        for(int i = 0; i < size; i++){
            taskcache.get(i).run();
        }

        taskcache.clear();

        events.GLPostDraw();

        finishFrame();
    }

    public static FSRThread getRenderThread(){
        return renderthread;
    }

    public static int getCurrentPassIndex(){
        return CURRENT_PASS_INDEX;
    }

    public static FSGlobal getGlobal(){
        return global;
    }

    public static void post(FSRTask task){
        synchronized(tasks){
            tasks.add(task);
        }
    }

    protected static void finishFrame(){
        FSCFrames.timeFrameEnded();
        FSCEGL.swapBuffers();
        global.notifyFrameSwap();
        FSCFrames.finalizeFrame();
    }

    protected static void notifyPaused(){
        global.notifyPaused();
    }

    protected static void notifyResumed(){
        global.notifyResumed();
    }

    protected static void destroy(){
        renderthread.lockdown();

        if(!FSControl.getDestroyOnPause()){
            FSR.notifyPaused();

        }else{
            renderthread.requestDestruction();
            renderthread = null;

            global.destroy();

            CURRENT_PASS_INDEX = -1;
            CURRENT_PASS_ENTRY_INDEX = -1;

            isInitialized = false;

            threadinterface = null;
            global = null;
            choreographer = null;
            tasks = null;
            taskcache = null;
        }
    }
}

