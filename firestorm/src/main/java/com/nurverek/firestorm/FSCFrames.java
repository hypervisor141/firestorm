package com.nurverek.firestorm;

import vanguard.VLLog;

public class FSCFrames{

    private static long GLOBAL_ID;
    private static long TOTAL_FRAMES;
    private static long FRAME_TIME;
    private static long AVERAGE_FRAMESWAP_TIME;
    private static long AVERAGE_PROCESS_TIME;
    private static long FRAME_SECOND_TRACKER;
    private static int FPS;

    private volatile static int MAX_UNCHANGED_FRAMES;
    private volatile static int MAX_QUEUED_FRAMES;
    private volatile static boolean EFFICIENT_RENDER;

    private static int UNCHANGED_FRAMES;
    private static int QUEUED_FRAMES_COUNT;
    private static int EXTERNAL_CHANGES;
    
    private static VLLog LOG;
    private final static Object CHANGELOCK = new Object();
    private final static Object STATLOCK = new Object();
    private final static Object IDLOCK = new Object();

    protected static void initialize(int maxunchangedframes, int maxqueuedframes){
        LOG = new VLLog(FSControl.LOGTAG, 1);
        
        synchronized(IDLOCK){
            GLOBAL_ID = 1000;
        }

        synchronized(STATLOCK){
            TOTAL_FRAMES = 0;
            FRAME_TIME = 0;
            AVERAGE_FRAMESWAP_TIME = 0;
            AVERAGE_PROCESS_TIME = 0;
            FRAME_SECOND_TRACKER = 0;
            FPS = 0;
        }

        MAX_UNCHANGED_FRAMES = maxunchangedframes;
        MAX_QUEUED_FRAMES = maxqueuedframes;
        EFFICIENT_RENDER = true;

        synchronized(CHANGELOCK){
            UNCHANGED_FRAMES = 0;
            QUEUED_FRAMES_COUNT = 0;
            EXTERNAL_CHANGES = 1;
        }
    }

    public static void addExternalChangesForFrame(int changes){
        synchronized(CHANGELOCK){
            EXTERNAL_CHANGES += changes;
        }

        if(changes > 0){
            requestFrame();
        }
    }

    public static void setEfficientRenderMode(boolean enable){
        EFFICIENT_RENDER = enable;
    }

    public static void setEfficientModeUnChangedFramesThreshold(int threshold){
        MAX_UNCHANGED_FRAMES = threshold;
    }

    public static void resetUnchangedFrames(){
        synchronized(CHANGELOCK){
            UNCHANGED_FRAMES = 0;
        }
    }

    public static long getNextID(){
        synchronized(IDLOCK){
            return GLOBAL_ID++;
        }
    }

    public static long getTotalFrames(){
        synchronized(STATLOCK){
            return TOTAL_FRAMES;
        }
    }

    public static long getFrameTime(){
        synchronized(STATLOCK){
            return FRAME_TIME;
        }
    }

    public static long getAverageFrameSwapTime(){
        synchronized(STATLOCK){
            return AVERAGE_FRAMESWAP_TIME;
        }
    }

    public static long getAverageFrameProcessTime(){
        synchronized(STATLOCK){
            return AVERAGE_PROCESS_TIME;
        }
    }

    public static long getCurrentFPS(){
        synchronized(STATLOCK){
            return FPS;
        }
    }

    public static long getUnchangedFrames(){
        synchronized(CHANGELOCK){
            return UNCHANGED_FRAMES;
        }
    }

    public static long getQueuedFrames(){
        synchronized(CHANGELOCK){
            return QUEUED_FRAMES_COUNT;
        }
    }

    public static long getMaxQueuedFrames(){
        return MAX_QUEUED_FRAMES;
    }

    public static long getMaxUnchangedFrames(){
        return MAX_UNCHANGED_FRAMES;
    }

    public static boolean getEfficientRenderMode(){
        return EFFICIENT_RENDER;
    }

    public static void requestFrame(){
        synchronized(CHANGELOCK){
            if(QUEUED_FRAMES_COUNT >= MAX_QUEUED_FRAMES){
                return;
            }

            UNCHANGED_FRAMES = 0;
            QUEUED_FRAMES_COUNT++;
        }

        FSR.requestFrame();
    }

    protected static void finalizeFrame(){
        timeBufferSwapped();

        synchronized(CHANGELOCK){
            QUEUED_FRAMES_COUNT--;

            if(EFFICIENT_RENDER){
                if(EXTERNAL_CHANGES == 0 && UNCHANGED_FRAMES < MAX_UNCHANGED_FRAMES){
                    UNCHANGED_FRAMES++;
                    FSR.requestFrame();
                }

                return;
            }
        }

        FSR.requestFrame();
    }

    protected static void timeFrameStarted(){
        FRAME_TIME = System.currentTimeMillis();

        if(FRAME_SECOND_TRACKER == 0){
            FRAME_SECOND_TRACKER = System.currentTimeMillis();
        }
    }

    protected static void timeFrameEnded(){
        synchronized(STATLOCK){
            AVERAGE_FRAMESWAP_TIME = (AVERAGE_FRAMESWAP_TIME + System.currentTimeMillis() - FRAME_TIME) / 2;
        }
    }

    protected static void timeBufferSwapped(){
        long now = System.currentTimeMillis();

        synchronized(STATLOCK){
            long tracker = now - FRAME_SECOND_TRACKER;

            FPS++;

            TOTAL_FRAMES++;

            AVERAGE_PROCESS_TIME = (AVERAGE_PROCESS_TIME + now - FRAME_TIME) / 2;

            if(tracker / 1000F >= 1){
                LOG.append("FPS[");
                LOG.append(FPS);
                LOG.append("] window[");
                LOG.append((tracker / 1000f));
                LOG.append("sec] totalFrames[");
                LOG.append(TOTAL_FRAMES);
                LOG.append("] avgFrameProcessing[");
                LOG.append(AVERAGE_FRAMESWAP_TIME);
                LOG.append("ms] avgFullFrame[");
                LOG.append(AVERAGE_PROCESS_TIME);
                LOG.append("ms]");
                LOG.printInfo();

                FRAME_SECOND_TRACKER = now;
                FPS = 0;
            }
        }
    }

    protected static void destroy(){
        if(!FSControl.getDestroyOnPause()){
            synchronized(IDLOCK){
                GLOBAL_ID = -1;
            }
            synchronized(STATLOCK){
                TOTAL_FRAMES = 0;
                AVERAGE_FRAMESWAP_TIME = 0;
                AVERAGE_PROCESS_TIME = 0;
                FRAME_SECOND_TRACKER = 0;
                FRAME_TIME = 0;
                FPS = 0;
            }

            MAX_UNCHANGED_FRAMES = -1;
            MAX_QUEUED_FRAMES = -1;
            EFFICIENT_RENDER = false;

            synchronized(CHANGELOCK){
                UNCHANGED_FRAMES = 0;
                QUEUED_FRAMES_COUNT = 0;
                EXTERNAL_CHANGES = 0;
            }
        }
    }
}
