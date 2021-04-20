package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTracker;
import vanguard.VLBufferTrackerDetailed;

public abstract class FSLinkBuffered<DATA, BUFFER extends VLBuffer<?, ?>, TRACKER extends VLBufferTrackerDetailed> extends FSLink<DATA>{

    public TRACKER tracker;
    public BUFFER buffer;

    public FSLinkBuffered(DATA data, TRACKER tracker, BUFFER buffer){
        super(data);

        this.tracker = tracker;
        this.buffer = buffer;
    }

    public FSLinkBuffered(DATA data, TRACKER tracker){
        super(data);
        this.tracker = tracker;
    }

    public void updateBuffer(){
        buffer(tracker.unitoffset(), tracker.unitsize(), tracker.unitsubcount(), tracker.stride());
    }

    public void setBuffer(BUFFER buffer){
        this.buffer = buffer;
    }

    public void setVertexBuffer(FSVertexBuffer<BUFFER> buffer){}

    public abstract void buffer(int unitoffset, int unitsize, int unitsubcount, int stride);
    public abstract int size();
}
