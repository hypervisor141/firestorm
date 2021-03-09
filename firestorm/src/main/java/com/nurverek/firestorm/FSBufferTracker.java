package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTracker;

public class FSBufferTracker<BUFFER extends VLBuffer<?, ?>> extends VLBufferTracker<BUFFER>{

    public FSVertexBuffer<BUFFER> buffer;

    public FSBufferTracker(FSVertexBuffer<BUFFER> buffer, int offset, int count){
        super(buffer.provider(), offset, count);
        this.buffer = buffer;
    }

    public FSBufferTracker(){

    }

    @Override
    public void buffer(BUFFER buffer){
        super.buffer(buffer);
        this.buffer.provider(buffer);
    }
}
