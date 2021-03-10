package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTrackerInterleaved;

public class FSBufferTrackerInterleaved<BUFFER extends VLBuffer<?, ?>> extends VLBufferTrackerInterleaved<BUFFER>{

    public FSVertexBuffer<BUFFER> buffer;

    public FSBufferTrackerInterleaved(FSVertexBuffer<BUFFER> buffer, int offset, int inputoffset, int count, int unitoffset, int unitsize, int unitsubcount, int stride){
        super(buffer.provider(), offset, inputoffset, count, unitoffset, unitsize, unitsubcount, stride);
        this.buffer = buffer;
    }

    public FSBufferTrackerInterleaved(){

    }

    @Override
    public void buffer(BUFFER buffer){
        super.buffer(buffer);
        this.buffer.provider(buffer);
    }
}
