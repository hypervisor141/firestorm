package com.nurverek.firestorm;

import vanguard.VLLog;

public class FSCFrames{

    private static long TOTAL_FRAMES;
    private static long FRAME_TIME;
    private static long AVERAGE_FRAMESWAP_TIME;
    private static long AVERAGE_PROCESS_TIME;
    private static long FRAME_SECOND_TRACKER;
    private static int FPS;

    private static int MAX_UNCHANGED_FRAMES;
    private static int MAX_QUEUED_FRAMES;
    private static boolean EFFICIENT_RENDER;
    private static int UNCHANGED_FRAMES;
    private static int QUEUED_FRAMES_COUNT;
    private static int EXTERNAL_CHANGES;
    
    private static VLLog LOG;
    private final static Object LOCK = new Object();

    protected static void initialize(int maxunchangedframes, int maxqueuedframes){
        LOG = new VLLog(FSControl.LOGTAG, 1);

        synchronized(LOCK){
            TOTAL_FRAMES = 0;
            FRAME_TIME = 0;
            AVERAGE_FRAMESWAP_TIME = 0;
            AVERAGE_PROCESS_TIME = 0;
            FRAME_SECOND_TRACKER = 0;
            FPS = 0;

            MAX_UNCHANGED_FRAMES = maxunchangedframes;
            MAX_QUEUED_FRAMES = maxqueuedframes;
            EFFICIENT_RENDER = true;
            UNCHANGED_FRAMES = 0;
            QUEUED_FRAMES_COUNT = 0;
            EXTERNAL_CHANGES = 1;
        }
    }

    public static void addExternalChangesForFrame(int changes){
        synchronized(LOCK){
            EXTERNAL_CHANGES += changes;
        }

        if(changes > 0){
            requestFrame();
        }
    }

    public static void setEfficientRenderMode(boolean enable){
        synchronized(LOCK){
            EFFICIENT_RENDER = enable;
        }
    }

    public static void setEfficientModeUnChangedFramesThreshold(int threshold){
        synchronized(LOCK){
            MAX_UNCHANGED_FRAMES = threshold;
        }
    }

    public static void resetUnchangedFrames(){
        synchronized(LOCK){
            UNCHANGED_FRAMES = 0;
        }
    }

    public static long getTotalFrames(){
        synchronized(LOCK){
            return TOTAL_FRAMES;
        }
    }

    public static long getFrameTime(){
        synchronized(LOCK){
            return FRAME_TIME;
        }
    }

    public static long getAverageFrameSwapTime(){
        synchronized(LOCK){
            return AVERAGE_FRAMESWAP_TIME;
        }
    }

    public static long getAverageFrameProcessTime(){
        synchronized(LOCK){
            return AVERAGE_PROCESS_TIME;
        }
    }

    public static long getCurrentFPS(){
        synchronized(LOCK){
            return FPS;
        }
    }

    public static long getUnchangedFrames(){
        synchronized(LOCK){
            return UNCHANGED_FRAMES;
        }
    }

    public static long getQueuedFrames(){
        synchronized(LOCK){
            return QUEUED_FRAMES_COUNT;
        }
    }

    public static long getMaxQueuedFrames(){
        synchronized(LOCK){
            return MAX_QUEUED_FRAMES;
        }
    }

    public static long getMaxUnchangedFrames(){
        synchronized(LOCK){
            return MAX_UNCHANGED_FRAMES;
        }
    }

    public static boolean getEfficientRenderMode(){
        synchronized(LOCK){
            return EFFICIENT_RENDER;
        }
    }

    public static void requestFrame(){
        synchronized(LOCK){
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
        boolean request = false;

        synchronized(LOCK){
            QUEUED_FRAMES_COUNT--;

            if(EFFICIENT_RENDER){
                if(EXTERNAL_CHANGES == 0){
                    if(UNCHANGED_FRAMES < MAX_UNCHANGED_FRAMES){
                        UNCHANGED_FRAMES++;
                        request = true;
                    }

                }else{
                    request = true;
                }

            }else{
                request = true;
            }

            EXTERNAL_CHANGES = 0;
        }

        if(request){
            FSR.requestFrame();
        }
    }

    protected static void timeFrameStarted(){
        synchronized(LOCK){
            FRAME_TIME = System.currentTimeMillis();

            if(FRAME_SECOND_TRACKER == 0){
                FRAME_SECOND_TRACKER = System.currentTimeMillis();
            }
        }
    }

    protected static void timeFrameEnded(){
        synchronized(LOCK){
            AVERAGE_FRAMESWAP_TIME = (AVERAGE_FRAMESWAP_TIME + System.currentTimeMillis() - FRAME_TIME) / 2;
        }
    }

    protected static void timeBufferSwapped(){
        long now = System.currentTimeMillis();

        synchronized(LOCK){
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
            synchronized(LOCK){
                TOTAL_FRAMES = 0;
                AVERAGE_FRAMESWAP_TIME = 0;
                AVERAGE_PROCESS_TIME = 0;
                FRAME_SECOND_TRACKER = 0;
                FRAME_TIME = 0;
                FPS = 0;

                MAX_UNCHANGED_FRAMES = -1;
                MAX_QUEUED_FRAMES = -1;
                EFFICIENT_RENDER = false;
                UNCHANGED_FRAMES = 0;
                QUEUED_FRAMES_COUNT = 0;
                EXTERNAL_CHANGES = 0;
            }
        }
    }
}
