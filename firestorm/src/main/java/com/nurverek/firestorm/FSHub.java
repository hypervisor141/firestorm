package com.nurverek.firestorm;

import android.content.Context;

import vanguard.VLListType;

public abstract class FSHub{

    protected FSBufferPool bufferpool;

    public FSHub(){

    }

    public void initialize(Context context){
        this.bufferpool = generateBufferPool();
        assemble(context, bufferpool, FSR.getRenderPasses());
    }

    protected abstract FSBufferPool generateBufferPool();
    protected abstract void assemble(Context context, FSBufferPool bufferpool, VLListType<FSRPass> passes);
    public abstract void paused();
    public abstract void resumed();

    protected void destroy(){
        bufferpool.destroy();
    }
}