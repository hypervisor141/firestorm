package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTracker;

public abstract class FSLinkBuffered<DATA, BUFFER extends VLBuffer<?, ?>, TRACKER extends VLBufferTracker<BUFFER>> extends FSLink<DATA>{

    public TRACKER tracker;

    public FSLinkBuffered(DATA data, TRACKER tracker){
        super(data);
        this.tracker = tracker;
    }

    public FSLinkBuffered(){

    }

    public abstract void buffer(BUFFER buffer, int unitoffset, int unitsize, int unitsubcount, int stride);

    public TRACKER tracker(){
        return tracker;
    }

    public abstract int size();
}
