package com.nurverek.firestorm;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES32;
import android.util.Log;

import com.nurverek.vanguard.VLDebug;

public final class FSControl {

    public static final String LOGTAG = "FIRESTORM";

    public static boolean DEBUG_GLOBALLY = true;
    public static final int DEBUG_FULL = 2;
    public static final int DEBUG_NORMAL = 1;
    public static final int DEBUG_DISABLED = 0;

    protected static FSViewConfig CONFIG;
    protected static FSSurface SURFACE;
    protected static FSEvents EVENTS;
    protected static GLSurfaceConfig SCONFIG;

    private static EGLDisplay GLDISPLAY;
    private static EGLSurface GLSURFACE;
    private static EGLContext GLCONTEXT;
    private static EGLConfig GLCONFIG;

    private static int CONTAINERWIDTH;
    private static int CONTAINERHEIGHT;
    private static float CONTAINERASPECTRATIO;

    private static int MAINWIDTH;
    private static int MAINHEIGHT;
    private static float MAINASPECTRATIO;

    private static int REALWIDTH;
    private static int REALHEIGHT;
    private static float REALASPECTRATIO;

    public static final int GL_UNCHANGED_FRAME_LIMIT = 5;
    private static long GLOBAL_ID = 1000;
    private static long TOTAL_FRAMES = 0;
    private static long FRAME_TIME;
    private static long AVERAGE_SWAPPED_TIME;
    private static long AVERAGE_PROCESS_TIME;
    private static long FRAME_SECOND_TRACKER;
    private static int FPS;
    private static int UNCHANGED_FRAMES;
    private static volatile boolean EFFICIENT_RENDERING;
    private static volatile boolean PERFORMANCE_MONITOR;

    private static volatile boolean isGLInitialized;
    protected static float[] CLEARCOLOR;

    protected static void initialize(FSEvents e, FSSurface s, GLSurfaceConfig sc){
        VLDebug.tag(LOGTAG);

        CLEARCOLOR = new float[4];

        SURFACE = s;
        EVENTS = e;
        CONFIG = new FSViewConfig();

        FPS = 0;
        FRAME_TIME = 0;
        UNCHANGED_FRAMES = 0;

        isGLInitialized = false;
        EFFICIENT_RENDERING = true;
        PERFORMANCE_MONITOR = false;

        setSurfaceConfig(sc);

        FSInput.initialize();
        FSRenderer.initialize();
    }

    protected static void initializeGL(boolean continuing){
        if(!continuing || GLCONTEXT == null || GLSURFACE == null){
            int[] vers = new int[2];
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfig = new int[1];

            GLDISPLAY = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            FSTools.checkEGLError("eglGetDisplay");

            EGL14.eglInitialize(GLDISPLAY, vers, 0, vers, 1);
            FSTools.checkEGLError("eglInitialize");

            EGL14.eglChooseConfig(GLDISPLAY, new int[]{
                    EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                    EGL14.EGL_LEVEL, 0,
                    EGL14.EGL_SAMPLES, 1,
                    EGL14.EGL_STENCIL_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 16,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_NONE
            }, 0, configs, 0, 1, numConfig, 0);
            FSTools.checkEGLError("eglChooseConfig");

            if(numConfig[0] == 0){
                throw new RuntimeException("Error loading a GL Configuration.");
            }

            GLCONFIG = configs[0];
            FSTools.checkEGLError("eglCreateWindowSurface");

            GLCONTEXT = EGL14.eglCreateContext(GLDISPLAY, GLCONFIG, EGL14.EGL_NO_CONTEXT, new int[]{
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
            }, 0);
            FSTools.checkEGLError("eglCreateContext");

        }else{
            EGL14.eglDestroySurface(GLDISPLAY, GLSURFACE);
            FSTools.checkEGLError("eglDestroySurface");
        }

        GLSURFACE = EGL14.eglCreateWindowSurface(GLDISPLAY, GLCONFIG, SURFACE.getHolder(), new int[]{ EGL14.EGL_NONE }, 0);
        FSTools.checkEGLError("eglCreateWindowSurface");

        EGL14.eglMakeCurrent(GLDISPLAY, GLSURFACE, GLSURFACE, GLCONTEXT);
        FSTools.checkEGLError("eglMakeCurrent");

        isGLInitialized = true;
    }

    public static void setClearColor(float r, float g, float b, float a){
        CLEARCOLOR[0] = r;
        CLEARCOLOR[1] = g;
        CLEARCOLOR[2] = b;
        CLEARCOLOR[3] = a;
    }

    protected static void setRealDimensions(int width, int height){
        REALWIDTH = width;
        REALHEIGHT = height;
        REALASPECTRATIO = (float)width / height;
    }

    protected static void setMainDimensions(int width, int height){
        MAINWIDTH = width;
        MAINHEIGHT = height;
        MAINASPECTRATIO = (float)width / height;
    }

    protected static void setContainerDimensions(int width, int height){
        CONTAINERWIDTH = width;
        CONTAINERHEIGHT = height;
        CONTAINERASPECTRATIO = (float)width / height;
    }

    public static GLSurfaceConfig setSurfaceConfig(GLSurfaceConfig config){
        if(config != null){
            SCONFIG = config;

        }else if(SCONFIG == null){
            SCONFIG = new GLSurfaceConfig();
        }

        return SCONFIG;
    }

    protected static void setSceneConfig(FSViewConfig s){
        CONFIG = s;
    }

    protected static void setSurface(FSSurface s){
        SURFACE = s;
    }

    public static void setRenderLimitControl(boolean enable){
        synchronized(FSRenderer.RENDERLOCK){
            EFFICIENT_RENDERING = enable;
        }
    }

    public static void setPerformanceMonitorMode(boolean enabled){
        synchronized(FSRenderer.RENDERLOCK){
            PERFORMANCE_MONITOR = enabled;
        }
    }

    public static void signalFrameRender(boolean continuous){
        SCONFIG.setRenderContinuously(continuous);

        if(continuous){
            SURFACE.postFrame();
        }
    }



    public static FSSurface getSurface(){
        return SURFACE;
    }

    public static FSActivity getActivity(){
        return SURFACE.getActivity();
    }

    public static Context getContext(){
        return SURFACE.getContext();
    }

    public static FSViewConfig getViewConfig(){
        return CONFIG;
    }

    public static GLSurfaceConfig getSurfaceConfig(){
        return SCONFIG;
    }

    public static boolean getPerformanceMonitorMode(){
        return PERFORMANCE_MONITOR;
    }

    public static long getTotalFrames(){
        return TOTAL_FRAMES;
    }

    public static boolean getEfficientRenderingMode(){
        return EFFICIENT_RENDERING;
    }

    public static long getNextID(){
        return GLOBAL_ID++;
    }

    public static int getContainerWidth(){
        return CONTAINERWIDTH;
    }

    public static int getContainerHeight(){
        return CONTAINERHEIGHT;
    }

    public static float getContainerAspectRatio(){
        return CONTAINERASPECTRATIO;
    }

    public static int getMainWidth(){
        return MAINWIDTH;
    }

    public static int getMainHeight(){
        return MAINHEIGHT;
    }

    public static float getMainAspectRatio(){
        return MAINASPECTRATIO;
    }

    public static int getRealWidth(){
        return REALWIDTH;
    }

    public static int getRealHeight(){
        return REALHEIGHT;
    }

    public static float getRealAspectRatio(){
        return REALASPECTRATIO;
    }

    public static float[] getClearColor(){
        return CLEARCOLOR;
    }

    public static boolean isIsGLInitialized(){
        return isGLInitialized;
    }




    public static void multiplyVP(float[] results, int offset, float[] point, int offset2){
        CONFIG.multiplyViewPerspective(results, offset, point, offset2);
    }

    public static void convertToMVP(float[] results, int offset, float[] model){
        CONFIG.convertToMVP(results, offset, model);
    }

    protected static void swapBuffers(){
        EGL14.eglSwapBuffers(GLDISPLAY, GLSURFACE);
        FSTools.checkEGLError("eglSwapBuffers");
        GLES32.glGetError();
    }

    protected static void checkRenderControl(int changes){
        if(EFFICIENT_RENDERING){
            if(changes == 0){
                if(UNCHANGED_FRAMES >= GL_UNCHANGED_FRAME_LIMIT && !SCONFIG.getNoLimitRenderControl()){
                    UNCHANGED_FRAMES = 0;
                    signalFrameRender(false);

                }else{
                    UNCHANGED_FRAMES++;
                    SURFACE.postFrame();
                }

            }else if(SCONFIG.getRenderContinuously()){
                SURFACE.postFrame();
            }

        }else if(SCONFIG.getRenderContinuously()){
            SURFACE.postFrame();
        }
    }

    protected static void timeFrameStarted(){
        FRAME_TIME = System.currentTimeMillis();

        if(FRAME_SECOND_TRACKER == 0){
            FRAME_SECOND_TRACKER = System.currentTimeMillis();
        }
    }

    protected static void timeFrameEnded(){
        AVERAGE_SWAPPED_TIME = (AVERAGE_SWAPPED_TIME + System.currentTimeMillis() - FRAME_TIME) / 2;
    }

    protected static void timeBufferSwapped(){
        long now = System.currentTimeMillis();
        long tracker = now - FRAME_SECOND_TRACKER;

        FPS++;
        TOTAL_FRAMES++;

        AVERAGE_PROCESS_TIME = (AVERAGE_PROCESS_TIME + now - FRAME_TIME) / 2;

        if(tracker / 1000F >= 1){
            Log.d(LOGTAG, "FPS[" + FPS + "] time[" + (tracker / 1000f) + "sec] totalFrames[" + TOTAL_FRAMES + "] averageProcessingTime[" + AVERAGE_SWAPPED_TIME + "ms] averageFullFrameTime[" + AVERAGE_PROCESS_TIME + "]");

            FRAME_SECOND_TRACKER = now;
            FPS = 0;
        }
    }

    private static void destroyGL(){
        EGL14.eglMakeCurrent(GLDISPLAY, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        FSTools.checkEGLError("eglMakeCurrent");

        if(!SCONFIG.getKeepAlive()){
            EGL14.eglDestroySurface(GLDISPLAY, GLSURFACE);
            FSTools.checkEGLError("eglDestroySurface");
            EGL14.eglDestroyContext(GLDISPLAY, GLCONTEXT);
            FSTools.checkEGLError("eglDestroyContext");
            EGL14.eglReleaseThread();
            FSTools.checkEGLError("eglMakeCurrent");
            EGL14.eglTerminate(GLDISPLAY);
            FSTools.checkEGLError("eglTerminate");
        }

        isGLInitialized = false;
    }

    protected static void destroy(){
        FSRenderer.RENDERTHREAD.shutdown();
        FSControl.destroyGL();

        if(!SCONFIG.getKeepAlive()){
            CONFIG = null;
            SCONFIG = null;
            GLCONTEXT = null;
            GLDISPLAY = null;
            SURFACE = null;

            EFFICIENT_RENDERING = false;

            FPS = 0;
            FRAME_TIME = 0;
            UNCHANGED_FRAMES = 0;

            FSInput.destroy();
            FSRenderer.destroy();
        }
    }

    public static final class GLSurfaceConfig{

        private boolean dirtyrender, nolimit, keepalive, touchable;

        public GLSurfaceConfig(){
            nolimit = false;
            keepalive = false;
            dirtyrender = false;

            touchable = true;
        }


        public void setTouchable(boolean s){
            touchable = s;
        }

        public void setKeepAlive(boolean s){
            keepalive = s;
        }

        public void setRenderContinuously(boolean s){
            dirtyrender = s;
        }

        public void setNoLimitContinousRender(boolean s){
            nolimit = s;
        }


        public boolean getKeepAlive(){
            return keepalive;
        }

        public boolean getNoLimitRenderControl(){
            return nolimit;
        }

        public boolean getRenderContinuously(){
            return dirtyrender;
        }

        public boolean getTouchable(){
            return touchable;
        }
    }
}
