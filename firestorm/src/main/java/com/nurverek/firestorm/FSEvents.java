package com.nurverek.firestorm;

public interface FSEvents{

    void GLPreSurfaceCreate(boolean continuing);
    void GLPostSurfaceCreate(boolean continuing);
    void GLPreSurfaceChange(int width, int height);
    void GLPostSurfaceChange(int width, int height);
    void GLPreSurfaceDestroy();
    void GLPostSurfaceDestroy();
    void GLPreCreated(boolean continuing);
    void GLPostCreated(boolean continuing);
    void GLPreChange(int width, int height);
    void GLPostChange(int width, int height);
    void GLPreDraw();
    void GLPostDraw();
    void GLPreAdvancement();
    void GLPostAdvancement(long changes);
}