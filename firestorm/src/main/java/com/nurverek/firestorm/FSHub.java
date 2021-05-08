package com.nurverek.firestorm;

import android.content.Context;

import vanguard.VLListType;

public abstract class FSHub{

    public FSHub(){

    }

    public void initialize(Context context){
        assemble(context, FSR.getRenderPasses());
    }

    public FSAutomator createAutomator(int filecapacity, int scancapacity, int buffercapacity){
        return new FSAutomator(filecapacity, scancapacity, buffercapacity);
    }

    protected abstract void assemble(Context context, VLListType<FSRPass> targets);

    public abstract void paused();
    public abstract void resumed();
    public abstract void destroy();
}