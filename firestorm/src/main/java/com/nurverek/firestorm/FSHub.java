package com.nurverek.firestorm;

import android.content.Context;

import vanguard.VLListType;

public abstract class FSHub{

    public FSHub(){

    }

    public void initialize(Context context){
        assemble(context, FSR.getRenderPasses());
    }

    protected abstract void assemble(Context context, VLListType<FSRPass> targets);
    public abstract void paused();
    public abstract void resumed();
    public abstract void destroy();
}