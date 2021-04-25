package com.nurverek.firestorm;

public class FSEvents{

    public void GLPreSurfaceCreate(boolean resuming){}
    public void GLPostSurfaceCreate(boolean resuming){}
    public void GLPreSurfaceChange(int format, int width, int height){}
    public void GLPostSurfaceChange(int format, int width, int height){}
    public void GLPreSurfaceDestroy(){}
    public void GLPostSurfaceDestroy(){}
    public void GLPreCreated(boolean continuing){}
    public void GLPostCreated(boolean continuing){}
    public void GLPreChange(int format, int width, int height){}
    public void GLPostChange(int format, int width, int height){}
    public void GLPreDraw(){}
    public void GLPostDraw(){}
}