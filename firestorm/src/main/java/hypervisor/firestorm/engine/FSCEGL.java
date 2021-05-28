package hypervisor.firestorm.engine;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES32;
import android.view.SurfaceHolder;

public class FSCEGL{

    private static EGLDisplay display;
    private static EGLSurface surface;
    private static EGLContext context;
    private static EGLConfig config;

    public static void initialize(SurfaceHolder holder, int[] attributes, boolean resuming){
        if(!resuming || context == null || surface == null){
            int[] vers = new int[2];
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfig = new int[1];

            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            FSTools.checkEGLError();

            EGL14.eglInitialize(display, vers, 0, vers, 1);
            FSTools.checkEGLError();

            EGL14.eglChooseConfig(display, attributes, 0, configs, 0, 1, numConfig, 0);
            FSTools.checkEGLError();

            if(numConfig[0] == 0){
                throw new RuntimeException("[Error loading a GL Configuration]");
            }

            config = configs[0];
            FSTools.checkEGLError();

            context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, new int[]{
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
            }, 0);
            FSTools.checkEGLError();

        }else{
            EGL14.eglDestroySurface(display, surface);
            FSTools.checkEGLError();
        }

        surface = EGL14.eglCreateWindowSurface(display, config, holder, new int[]{ EGL14.EGL_NONE }, 0);
        FSTools.checkEGLError();

        EGL14.eglMakeCurrent(display, surface, surface, context);
        FSTools.checkEGLError();
    }

    public static void swapBuffers(){
        EGL14.eglSwapBuffers(display, surface);
        FSTools.checkEGLError();

        GLES32.glGetError();
    }

    public static void destroy(){
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        FSTools.checkEGLError();

        if(FSControl.getDestroyOnPause()){
            EGL14.eglDestroySurface(display, surface);
            FSTools.checkEGLError();

            EGL14.eglDestroyContext(display, context);
            FSTools.checkEGLError();

            EGL14.eglReleaseThread();
            FSTools.checkEGLError();

            EGL14.eglTerminate(display);
            FSTools.checkEGLError();

            display = null;
            surface = null;
            context = null;
            config = null;
        }
    }
}
