package com.nurverek.firestorm;

import android.content.Context;

import vanguard.VLListType;

public abstract class FSHub{

    public FSHub(){

    }

    public void initialize(Context context){
        assemble(context);
    }

    protected abstract void assemble(Context context);
    protected abstract void paused();
    protected abstract void resumed();
    protected abstract void destroy();
}