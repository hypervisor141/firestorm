package com.firestorm.engine;

import android.content.Context;

public class FSEvents{

    public void GLPreSurfaceCreate(FSSurface surface, Context context, boolean resuming){}
    public void GLPostSurfaceCreate(FSSurface surface, Context context, boolean resuming){}
    public void GLPreSurfaceChange(FSSurface surface, Context context, int format, int width, int height){}
    public void GLPostSurfaceChange(FSSurface surface, Context context, int format, int width, int height){}
    public void GLPreSurfaceDestroy(FSSurface surface, Context context){}
    public void GLPostSurfaceDestroy(FSSurface surface, Context context){}
    public void GLPreCreated(FSSurface surface, Context context, boolean continuing){}
    public void GLPostCreated(FSSurface surface, Context context, boolean continuing){}
    public void GLPreChange(FSSurface surface, Context context, int format, int width, int height){}
    public void GLPostChange(FSSurface surface, Context context, int format, int width, int height){}
    public void GLPreDraw(){}
    public void GLPostDraw(){}
}