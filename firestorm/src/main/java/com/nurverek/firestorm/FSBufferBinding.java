package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferFloat;
import vanguard.VLBufferTracker;

public class FSBufferBinding<BUFFER extends VLBuffer<?, ?>>{

    public BUFFER buffer;
    public FSVertexBuffer<BUFFER> vbuffer;
    public VLBufferTracker tracker;

    public FSBufferBinding(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, VLBufferTracker tracker){
        this.vbuffer = vbuffer;
        this.buffer = buffer;
        this.tracker = tracker;
    }

    public FSBufferBinding(){

    }

    public void updateVertexBuffer(){
        vbuffer.update();
    }

    public void updateVertexBufferStrict(){
        int offset = tracker.offset;
        vbuffer.update(offset,  tracker.endposition - offset);
    }
}
