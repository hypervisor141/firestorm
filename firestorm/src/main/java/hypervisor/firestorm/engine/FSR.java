package hypervisor.firestorm.engine;

import android.content.Context;
import android.view.Choreographer;

import hypervisor.vanguard.concurrency.VLThreadTaskType;
import hypervisor.vanguard.list.VLListType;

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
    private static Choreographer choreographer;
    private static volatile FSRThread renderthread;

    private static VLListType<FSRTask> tasks;
    private static VLListType<FSRTask> taskcache;

    public static final Object TASKLOCK = new Object();

    protected static boolean isInitialized;

    public static int CURRENT_PASS_INDEX;
    public static int CURRENT_PASS_ENTRY_INDEX;

    protected static void initialize(FSRInterface threadsrc){
        FSR.threadinterface = threadsrc;

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

        if(!continuing){
            FSGlobal.get().initialize(context);
        }

        events.GLPostCreated(surface, context, continuing);

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

        VLListType<FSRPass> passes = FSGlobal.get().passes;

        int size = passes.size();

        for(int i = 0; i < size; i++){
            CURRENT_PASS_INDEX = i;
            CURRENT_PASS_ENTRY_INDEX = -1;

            passes.get(i).draw();
        }

        synchronized(TASKLOCK){
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

    public static void post(FSRTask task){
        synchronized(TASKLOCK){
            tasks.add(task);
        }

        requestFrame();
    }

    protected static void finishFrame(){
        FSCFrames.timeFrameEnded();
        FSCEGL.swapBuffers();
        FSGlobal.get().notifyFrameSwap();
        FSCFrames.finalizeFrame();
    }

    protected static void notifyPaused(){
        FSGlobal.get().notifyPaused();
    }

    protected static void notifyResumed(){
        FSGlobal.get().notifyResumed();
    }

    protected static void destroy(boolean destroyonpause){
        renderthread.lockdown();

        if(!destroyonpause){
            FSR.notifyPaused();

        }else{
            renderthread.requestDestruction();
            renderthread = null;

            CURRENT_PASS_INDEX = -1;
            CURRENT_PASS_ENTRY_INDEX = -1;

            isInitialized = false;

            threadinterface = null;
            choreographer = null;
            tasks = null;
            taskcache = null;
        }
    }
}

