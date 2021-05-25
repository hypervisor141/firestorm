package com.firestorm.engine;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

public final class FSCDimensions{

    protected static int mainw;
    protected static int mainh;
    protected static float mainasp;

    protected static int realw;
    protected static int realh;
    protected static float realasp;

    protected static int surfacew;
    protected static int surfaceh;
    protected static float surfaceasp;

    protected static float density;

    public static void initialize(Activity act){
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = act.getWindowManager().getDefaultDisplay();

        display.getMetrics(metrics);
        setMainDimensions(metrics.widthPixels, metrics.heightPixels);

        display.getRealMetrics(metrics);
        setRealDimensions(metrics.widthPixels, metrics.heightPixels);

        density = act.getResources().getDisplayMetrics().density;
    }

    protected static void setRealDimensions(int width, int height){
        realw = width;
        realh = height;
        realasp = (float)width / height;
    }

    protected static void setMainDimensions(int width, int height){
        mainw = width;
        mainh = height;
        mainasp = (float)width / height;
    }

    protected static void setSurfaceDimensions(int width, int height){
        surfacew = width;
        surfaceh = height;
        surfaceasp = (float)width / height;
    }

    protected static void setDensity(float d){
        density = d;
    }

    public static int getSurfaceWidth(){
        return surfacew;
    }

    public static int getSurfaceHeight(){
        return surfaceh;
    }

    public static float getSurfaceAspectRatio(){
        return surfaceasp;
    }

    public static int getMainWidth(){
        return mainw;
    }

    public static int getMainHeight(){
        return mainh;
    }

    public static float getMainAspectRatio(){
        return mainasp;
    }

    public static int getRealWidth(){
        return realw;
    }

    public static int getRealHeight(){
        return realh;
    }

    public static float getRealAspectRatio(){
        return realasp;
    }
    
    protected static void destroy(){
        mainw = 0;
        mainh = 0;
        mainasp = 0;
        realw = 0;
        realh = 0;
        realasp = 0;
        surfacew = 0;
        surfaceh = 0;
        surfaceasp = 0;
        density = 0;
    }
}
