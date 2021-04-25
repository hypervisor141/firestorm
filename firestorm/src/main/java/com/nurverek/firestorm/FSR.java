package com.nurverek.firestorm;

import android.view.Choreographer;

import vanguard.VLListType;

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
            post(FSRThread.DRAW_FRAME, null);
        }
    };

    public static final Object RENDERLOCK = new Object();

    private static VLListType<FSRPass> passes;
    private static VLListType<FSHub> hubs;
    private static VLListType<FSRTask> tasks;
    private static VLListType<FSRTask> taskcache;

    private static FSRInterface threadinterface;
    private static Choreographer choreographer;
    private static volatile FSRThread renderthread;

    protected static boolean isInitialized;

    public static int CURRENT_PASS_INDEX;
    public static int CURRENT_PASS_ENTRY_INDEX;

    protected static void initialize(FSRInterface threadsrc){
        threadinterface = threadsrc;
        choreographer = Choreographer.getInstance();

        passes = new VLListType<>(10, 10);
        hubs = new VLListType<>(10, 100);
        tasks = new VLListType<>(10, 100);
        taskcache = new VLListType<>(10, 100);

        isInitialized = true;

        CURRENT_PASS_INDEX = 0;
        CURRENT_PASS_ENTRY_INDEX = 0;
    }

    public static void setFSRThreadInterface(FSRInterface threadsrc){
        threadinterface = threadsrc;
    }

    protected static void prepare(){
        renderthread = threadinterface.create();
        renderthread.setPriority(8);
        renderthread.initiate();
    }

    protected static void requestFrame(){
        if(renderthread != null && renderthread.running()){
            choreographer.postFrameCallback(CHOREOGRAPHER_CALLBACK);
        }
    }

    protected static FSRThread post(int code, Object d){
        if(renderthread != null){
            renderthread.post(code, d);
        }

        return renderthread;
    }

    protected static void onSurfaceCreated(boolean continuing){
        FSEvents events = FSControl.getSurface().events();

        events.GLPreCreated(continuing);
        events.GLPostCreated(continuing);
    }

    protected static void onSurfaceChanged(int width, int height){
        FSEvents events = FSControl.getSurface().events();

        events.GLPreChange(width, height);
        events.GLPostChange(width, height);

        requestFrame();
    }

    protected static void onDrawFrame(){
        FSCFrames.timeFrameStarted();

        synchronized(RENDERLOCK){
            FSEvents events = FSControl.getSurface().events();
            events.GLPreDraw();

            int size = passes.size();

            for(int i = 0; i < size; i++){
                CURRENT_PASS_INDEX = i;
                CURRENT_PASS_ENTRY_INDEX = -1;

                passes.get(i).draw();
            }

            events.GLPostDraw();

            synchronized(tasks){
                taskcache.add(tasks);
                tasks.clear();
            }

            size = taskcache.size();

            for(int i = 0; i < size; i++){
                taskcache.get(i).run();
            }

            taskcache.clear();
        }

        finishFrame();
    }

    public static void addRenderPass(FSRPass pass){
        passes.add(pass);
    }

    public static void addHub(FSHub hub){
        hubs.add(hub);
    }

    public static FSRThread getRenderThread(){
        return renderthread;
    }

    public static int getCurrentPassIndex(){
        return CURRENT_PASS_INDEX;
    }

    public static FSRPass getRenderPass(int index){
        return passes.get(index);
    }

    public static FSHub getHub(int index){
        return hubs.get(index);
    }

    public static VLListType<FSRPass> getRenderPasses(){
        return passes;
    }

    public static void post(FSRTask task){
        synchronized(tasks){
            tasks.add(task);
        }
    }

    public static void removeRenderPass(FSRPass pass){
        int size = passes.size();

        for(int i = 0; i < size; i++){
            if(passes.get(i).id() == pass.id()){
                passes.remove(i);
            }
        }
    }

    public static FSRPass removeRenderPass(int index){
        FSRPass item = passes.get(index);
        passes.remove(index);

        return item;
    }

    public static FSHub removeHub(int index){
        FSHub item = hubs.get(index);
        hubs.remove(index);

        return item;
    }

    public static int getRenderPassesSize(){
        return passes.size();
    }

    public static int getHubsSize(){
        return hubs.size();
    }

    protected static void finishFrame(){
        FSCFrames.timeFrameEnded();
        FSCEGL.swapBuffers();

        int size = passes.size();

        for(int i = 0; i < size; i++){
            passes.get(i).noitifyPostFrameSwap();
        }

        FSCFrames.finalizeFrame();
    }

    protected static void paused(){
        int size = hubs.size();

        for(int i = 0; i < size; i++){
            hubs.get(i).paused();
        }
    }

    protected static void resumed(){
        int size = hubs.size();

        for(int i = 0; i < size; i++){
            hubs.get(i).resumed();
        }
    }

    protected static void destroy(){
        renderthread.shutdown();
        renderthread = null;

        if(!FSControl.getKeepAlive()){
            synchronized(RENDERLOCK){
                int size = hubs.size();

                for(int i = 0; i < size; i++){
                    hubs.get(i).destroy();
                }

                CURRENT_PASS_INDEX = -1;
                CURRENT_PASS_ENTRY_INDEX = -1;

                isInitialized = false;

                threadinterface = null;
                choreographer = null;
                passes = null;
                tasks = null;
                taskcache = null;
            }

        }else{
            FSR.paused();
        }
    }
}

