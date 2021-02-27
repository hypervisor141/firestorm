package com.nurverek.firestorm;

import android.util.Log;

public class FSRControl{

    private static int EFFICIENT_MODE_UNCHANGED_FRAMES_THRESHOLD = 5;
    private static long GLOBAL_ID = 1000;
    private static long TOTAL_FRAMES = 0;
    private static long FRAME_TIME;
    private static long AVERAGE_FRAMESWAP_TIME;
    private static long AVERAGE_PROCESS_TIME;
    private static long FRAME_SECOND_TRACKER;
    private static int FPS;
    private static int UNCHANGED_FRAMES;

    private static volatile boolean EFFICIENT_RENDER;
    private static volatile boolean PERFORMANCE_MONITOR;

    protected static void initialize(){
        FPS = 0;
        FRAME_TIME = 0;
        UNCHANGED_FRAMES = 0;
        EFFICIENT_RENDER = true;
        PERFORMANCE_MONITOR = false;
    }

    public static void setEfficientRenderMode(boolean enable){
        synchronized(FSR.RENDERLOCK){
            EFFICIENT_RENDER = enable;
        }
    }

    public static void setPerformanceMonitorMode(boolean enabled){
        synchronized(FSR.RENDERLOCK){
            PERFORMANCE_MONITOR = enabled;
        }
    }

    public static void setEfficientModeUnChangedFramesThreshold(){

    }

    public static long getNextID(){
        return GLOBAL_ID++;
    }

    public static long getTotalFrames(){
        return TOTAL_FRAMES;
    }

    public static long getFrameTime(){
        return FRAME_TIME;
    }

    public static long getAverageFrameSwapTime(){
        return AVERAGE_FRAMESWAP_TIME;
    }

    public static long getAverageFrameProcessTime(){
        return AVERAGE_PROCESS_TIME;
    }

    public static long getCurrentFPS(){
        return FPS;
    }

    public static long getCurrentUnchangedFrames(){
        return UNCHANGED_FRAMES;
    }

    public static long getEfficientModeUnChangedFramesThreshold(){
        return EFFICIENT_MODE_UNCHANGED_FRAMES_THRESHOLD;
    }

    public static boolean getEfficientRenderMode(){
        return EFFICIENT_RENDER;
    }

    public static boolean getPerformanceMonitorMode(){
        return PERFORMANCE_MONITOR;
    }

    public static void signalFrameRender(boolean continuous){
        FSControl.surface.config().setRenderContinuously(continuous);

        if(continuous){
            FSControl.surface.postFrame();
        }
    }

    protected static void processFrameAndSignalNextFrame(int changes){
        FSSurface surface = FSControl.getSurface();
        FSSurface.Config config = FSControl.getSurface().config();

        if(EFFICIENT_RENDER){
            if(changes == 0){

                if(UNCHANGED_FRAMES >= EFFICIENT_MODE_UNCHANGED_FRAMES_THRESHOLD){
                    UNCHANGED_FRAMES = 0;
                    signalFrameRender(false);

                }else{
                    UNCHANGED_FRAMES++;
                    surface.postFrame();
                }

            }else if(config.getRenderContinuously()){
                surface.postFrame();
            }

        }else if(config.getRenderContinuously()){
            surface.postFrame();
        }
    }

    protected static void timeFrameStarted(){
        FRAME_TIME = System.currentTimeMillis();

        if(FRAME_SECOND_TRACKER == 0){
            FRAME_SECOND_TRACKER = System.currentTimeMillis();
        }
    }

    protected static void timeFrameEnded(){
        AVERAGE_FRAMESWAP_TIME = (AVERAGE_FRAMESWAP_TIME + System.currentTimeMillis() - FRAME_TIME) / 2;
    }

    protected static void timeBufferSwapped(){
        long now = System.currentTimeMillis();
        long tracker = now - FRAME_SECOND_TRACKER;

        FPS++;
        TOTAL_FRAMES++;

        AVERAGE_PROCESS_TIME = (AVERAGE_PROCESS_TIME + now - FRAME_TIME) / 2;

        if(tracker / 1000F >= 1){
            Log.d(FSControl.LOGTAG, "FPS[" + FPS + "] time[" + (tracker / 1000f) + "sec] totalFrames[" + TOTAL_FRAMES + "] averageProcessingTime[" + AVERAGE_FRAMESWAP_TIME + "ms] averageFullFrameTime[" + AVERAGE_PROCESS_TIME + "ms]");

            FRAME_SECOND_TRACKER = now;
            FPS = 0;
        }
    }

    protected static void destroy(){
        if(!FSControl.getKeepAlive()){
            EFFICIENT_MODE_UNCHANGED_FRAMES_THRESHOLD = 0;
            GLOBAL_ID = -1;
            TOTAL_FRAMES = 0;
            AVERAGE_FRAMESWAP_TIME = 0;
            AVERAGE_PROCESS_TIME = 0;
            FRAME_SECOND_TRACKER = 0;
            FRAME_TIME = 0;
            FPS = 0;
            UNCHANGED_FRAMES = 0;

            EFFICIENT_RENDER = false;
            PERFORMANCE_MONITOR = false;
        }
    }
}
