package com.nurverek.firestorm;

import vanguard.VLListType;

public class FSR{

    public static final FSRInterface DEFAULT_INTERFACE = new FSRInterface(){

        @Override
        public FSRThread create(){
            return new FSRThread();
        }
    };

    public static final Object RENDERLOCK = new Object();

    private static VLListType<FSRPass> passes;
    private static VLListType<FSRTask> tasks;
    private static VLListType<FSRTask> taskcache;

    private static FSRInterface threadinterface;
    private static FSRThread renderthread;

    protected static boolean isInitialized;

    public static int CURRENT_PASS_INDEX;
    public static int CURRENT_PASS_ENTRY_INDEX;

    protected static void initialize(FSRInterface threadsrc){
        threadinterface = threadsrc;

        passes = new VLListType<>(10, 10);
        tasks = new VLListType<>(10, 100);
        taskcache = new VLListType<>(10, 100);

        isInitialized = true;

        CURRENT_PASS_INDEX = 0;
        CURRENT_PASS_ENTRY_INDEX = 0;
    }

    public static void setFSRThreadInterface(FSRInterface threadsrc){
        threadinterface = threadsrc;
    }

    protected static void startRenderThread(){
        renderthread = threadinterface.create();
        renderthread.setPriority(Thread.MAX_PRIORITY);
        renderthread.initialize();
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

    protected static FSRThread getRenderThread(){
        return renderthread;
    }

    public static int getCurrentPassIndex(){
        return CURRENT_PASS_INDEX;
    }

    public static FSRPass getRenderPass(int index){
        return passes.get(index);
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

    public static int getRenderPassesSize(){
        return passes.size();
    }

    protected static void finishFrame(){
        FSCFrames.timeFrameEnded();
        FSCEGL.swapBuffers();

        int size = passes.size();

        for(int i = 0; i < size; i++){
            passes.get(i).noitifyPostFrameSwap();
        }

        FSCFrames.timeBufferSwapped();
        FSCFrames.processFrameAndSignalNextFrame();
    }

    protected static void destroy(){
        renderthread.shutdown();

        if(!FSControl.getKeepAlive()){
            int size = passes.size();

            for(int i = 0; i < size; i++){
                passes.get(i).destroy();
            }

            CURRENT_PASS_INDEX = -1;
            CURRENT_PASS_ENTRY_INDEX = -1;

            isInitialized = false;

            passes = null;
            tasks = null;
            taskcache = null;
            threadinterface = null;
            renderthread = null;
        }
    }
}

