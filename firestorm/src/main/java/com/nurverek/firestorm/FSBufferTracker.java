package com.nurverek.firestorm;

import vanguard.VLBufferTrackerType;

public class FSBufferTracker<BUFFER extends FSVertexBuffer<?>> extends VLBufferTrackerType<BUFFER>{

    protected int inputoffest;
    protected int unitoffset;
    protected int unitsize;
    protected int unitsubcount;
    protected int stride;

    public FSBufferTracker(BUFFER buffer, int offset, int inputoffest, int count, int unitoffset, int unitsize, int unitsubcount, int stride){
        super(buffer, offset, count);

        this.inputoffest = inputoffest;
        this.unitoffset = unitoffset;
        this.unitsize = unitsize;
        this.unitsubcount = unitsubcount;
        this.stride = stride;
    }

    public FSBufferTracker(){
    }

    public void inputoffest(int inputoffest){
        this.inputoffest = inputoffest;
    }

    public void unitoffset(int unitoffset){
        this.unitoffset = unitoffset;
    }

    public void unitsize(int unitsize){
        this.unitsize = unitsize;
    }

    public void unitsubcount(int unitsubcount){
        this.unitsubcount = unitsubcount;
    }

    public void stride(int stride){
        this.stride = stride;
    }

    public int inputoffest(){
        return this.inputoffest;
    }

    public int unitoffset(){
        return this.unitoffset;
    }

    public int unitsize(){
        return this.unitsize;
    }

    public int unitsubcount(){
        return this.unitsubcount;
    }

    public int stride(){
        return this.stride;
    }

    public void stringify(StringBuilder src, Object hint){
        src.append("[");
        src.append(getClass().getSimpleName());
        src.append("] buffer[");
        src.append(buffer.getClass().getSimpleName());
        src.append("] offset[");
        src.append(offset);
        src.append("] count[");
        src.append(count);
        src.append("] unitOffset[");
        src.append(unitoffset);
        src.append("] unitSize[");
        src.append(unitsize);
        src.append("] unitSubCount[");
        src.append(unitsubcount);
        src.append("] stride[");
        src.append(stride);
        src.append("] content[ ");
        
        buffer.stringify(src, hint);
        
        src.append(" ]");
    }
}
