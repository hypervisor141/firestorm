package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTracker;

public abstract class FSLinkBuffered<DATA, BUFFER extends VLBuffer<?, ?>> extends FSLink<DATA>{

    public VLBufferTracker tracker;
    public BUFFER buffer;

    public FSLinkBuffered(DATA data, BUFFER buffer){
        super(data);

        this.tracker = new VLBufferTracker();
        this.buffer = buffer;
    }

    public FSLinkBuffered(DATA data){
        super(data);
        this.tracker = new VLBufferTracker();
    }

    public void setBuffer(BUFFER buffer){
        this.buffer = buffer;
    }
    public void setVertexBuffer(FSVertexBuffer<BUFFER> buffer){}

    public abstract void buffer(int unitoffset, int unitsize, int unitsubcount, int stride);
    public abstract void buffer();
    public abstract void update();
    public abstract int size();
}
