package com.nurverek.firestorm;

import android.content.Context;

import vanguard.VLListType;

public abstract class FSHub{

    public FSHub(){

    }

    public void initialize(Context context){
        assemble(context, FSR.getRenderPasses());
    }

    protected abstract FSBufferPool generateBufferPool();
    protected abstract void assemble(Context context, VLListType<FSRPass> passes);
    protected abstract void paused();
    protected abstract void resumed();
    protected abstract void destroy();
}