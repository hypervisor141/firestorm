package com.firestorm.engine;

import android.content.Context;

public abstract class FSHub{

    public FSHub(){

    }

    public void initialize(Context context){
        assemble(context, FSR.getGlobal());
    }

    protected abstract void assemble(Context context, FSGlobal global);
    protected abstract void paused();
    protected abstract void resumed();
    protected abstract void destroy();
}