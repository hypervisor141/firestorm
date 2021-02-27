package com.nurverek.firestorm;

public class FSEvents{

    public void GLPreSurfaceCreate(boolean continuing){}
    public void GLPostSurfaceCreate(boolean continuing){}
    public void GLPreSurfaceChange(int width, int height){}
    public void GLPostSurfaceChange(int width, int height){}
    public void GLPreSurfaceDestroy(){}
    public void GLPostSurfaceDestroy(){}
    public void GLPreCreated(boolean continuing){}
    public void GLPostCreated(boolean continuing){}
    public void GLPreChange(int width, int height){}
    public void GLPostChange(int width, int height){}
    public void GLPreDraw(){}
    public void GLPostDraw(){}
    public void GLPreAdvancement(){}
    public void GLPostAdvancement(long changes){}
}