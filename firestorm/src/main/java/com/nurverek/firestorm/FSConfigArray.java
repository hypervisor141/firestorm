package com.nurverek.firestorm;

import vanguard.VLArray;

public abstract class FSConfigArray<TYPE extends VLArray<?, ?>> extends FSConfigLocated{

    protected TYPE array;
    protected int offset;
    protected int count;

    public FSConfigArray(Policy policy, TYPE array, int offset, int count){
        super(policy);

        this.array = array;
        this.offset = offset;
        this.count = count;
    }

    public FSConfigArray(TYPE array, int offset, int count){
        this.array = array;
        this.offset = offset;
        this.count = count;
    }

    public final void offset(int s){
        offset = s;
    }

    public final void count(int s){
        offset = s;
    }

    public final void array(TYPE array){
        this.array = array;
    }


    public final int offset(){
        return offset;
    }

    public final int count(){
        return count;
    }

    public int instance(){
        return -1;
    }

    public int element(){
        return -1;
    }

    public final TYPE array(){
        return array;
    }

    @Override
    public abstract void debugInfo(FSP program, FSMesh mesh, int debug);

    @Override
    public int getGLSLSize(){
        return count;
    }
}
