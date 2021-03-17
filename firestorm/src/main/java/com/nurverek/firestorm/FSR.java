package com.nurverek.firestorm;

import android.opengl.GLES32;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class FSR{

    public static final Object RENDERLOCK = new Object();
    public static final FSRInterface DEFAULT_INTERFACE = new FSRInterface(){

        @Override
        public FSRThread create(){
            return new FSRThread();
        }
    };

    private static ArrayList<FSRPass> passes;

    private static FSRInterface threadinterface;
    private static FSRThread renderthread;

    protected static boolean isInitialized;

    protected static int CURRENT_RENDER_PASS_INDEX;
    protected static int CURRENT_FSG_INDEX;
    protected static int CURRENT_PROGRAM_SET_INDEX;

    protected static void initialize(FSRInterface threadsrc){
        threadinterface = threadsrc;

        passes = new ArrayList<>();

        isInitialized = true;

        CURRENT_RENDER_PASS_INDEX = 0;
        CURRENT_FSG_INDEX = 0;
        CURRENT_PROGRAM_SET_INDEX = 0;
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
        synchronized(RENDERLOCK){
            int size = passes.size();

            for(int i = 0; i < size; i++){
                CURRENT_RENDER_PASS_INDEX = i;
                CURRENT_PROGRAM_SET_INDEX = -1;
                CURRENT_FSG_INDEX = -1;

                passes.get(i).execute();
            }

            finishFrame();
        }
    }

    public static void addRenderPass(FSRPass pass){
        passes.add(pass);
    }

    protected static FSRThread getRenderThread(){
        return renderthread;
    }

    public static int getCurrentRenderPassIndex(){
        return CURRENT_RENDER_PASS_INDEX;
    }

    public static FSRPass getRenderPass(int index){
        return passes.get(index);
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
        return passes.remove(index);
    }

    public static int getRenderPassesSize(){
        return passes.size();
    }

    protected static void finishFrame(){
        FSRFrames.timeFrameEnded();
        FSEGL.swapBuffers();

        int size = passes.size();

        for(int i = 0; i < size; i++){
            passes.get(i).noitifyPostFrameSwap();
        }

        FSRFrames.timeBufferSwapped();
        FSRFrames.processFrameAndSignalNextFrame();
    }

    protected static void destroy(){
        renderthread.shutdown();

        if(!FSControl.getKeepAlive()){
            int size = passes.size();

            for(int i = 0; i < size; i++){
                passes.get(i).destroy();
            }

            CURRENT_RENDER_PASS_INDEX = -1;
            CURRENT_PROGRAM_SET_INDEX = -1;
            CURRENT_FSG_INDEX = -1;

            isInitialized = false;

            passes = null;
        }
    }
}

