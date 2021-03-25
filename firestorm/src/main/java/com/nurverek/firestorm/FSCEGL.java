package com.nurverek.firestorm;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES32;
import android.view.SurfaceHolder;

public class FSCEGL{

    private static EGLDisplay display;
    private static EGLSurface surface;
    private static EGLContext context;
    private static EGLConfig config;

    public static void initialize(SurfaceHolder holder, boolean resuming){
        if(!resuming || context == null || surface == null){
            int[] vers = new int[2];
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfig = new int[1];

            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            FSTools.checkEGLError("eglGetDisplay");

            EGL14.eglInitialize(display, vers, 0, vers, 1);
            FSTools.checkEGLError("eglInitialize");

            EGL14.eglChooseConfig(display, new int[]{
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

            config = configs[0];
            FSTools.checkEGLError("eglCreateWindowSurface");

            context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, new int[]{
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
            }, 0);
            FSTools.checkEGLError("eglCreateContext");

        }else{
            EGL14.eglDestroySurface(display, surface);
            FSTools.checkEGLError("eglDestroySurface");
        }

        surface = EGL14.eglCreateWindowSurface(display, config, holder, new int[]{ EGL14.EGL_NONE }, 0);
        FSTools.checkEGLError("eglCreateWindowSurface");

        EGL14.eglMakeCurrent(display, surface, surface, context);
        FSTools.checkEGLError("eglMakeCurrent");
    }

    public static void swapBuffers(){
        EGL14.eglSwapBuffers(display, surface);
        FSTools.checkEGLError("eglSwapBuffers");

        GLES32.glGetError();
    }

    public static void destroy(){
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        FSTools.checkEGLError("eglMakeCurrent");

        if(!FSControl.getKeepAlive()){
            EGL14.eglDestroySurface(display, surface);
            FSTools.checkEGLError("eglDestroySurface");

            EGL14.eglDestroyContext(display, context);
            FSTools.checkEGLError("eglDestroyContext");

            EGL14.eglReleaseThread();
            FSTools.checkEGLError("eglMakeCurrent");

            EGL14.eglTerminate(display);
            FSTools.checkEGLError("eglTerminate");

            display = null;
            surface = null;
            context = null;
            config = null;
        }
    }
}
