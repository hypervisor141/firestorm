package com.nurverek.firestorm;

import vanguard.VLBTracker;
import vanguard.VLBufferAddress;
import vanguard.VLBufferTracker;
import vanguard.VLBufferTrackerType;

public class FSBTracker extends VLBTracker{

    public FSVertexBuffer buffer;

    public FSBTracker(FSVertexBuffer buffer, int offset, int unitoffset, int unitsize, int stride, int count){
        super(offset, unitoffset, unitsize, stride, count);
        this.buffer = buffer;
    }

    public FSBTracker(){

    }

    @Override
    public void stringify(StringBuilder src, Object hint){
        src.append("[");
        src.append(getClass().getSimpleName());
        src.append("] buffer[");
        src.append(buffer.getClass().getSimpleName());
        src.append("] offset[");
        src.append(offset);
        src.append("] unitSize[");
        src.append(unitsize);
        src.append("] unitOffset[");
        src.append(unitoffset);
        src.append("] stride[");
        src.append(stride);
        src.append("] count[");
        src.append(count);
        src.append("] bindPoint[");
        src.append(buffer.bindPoint());
        src.append("] content[ ");

        buffer.provider().stringify(src, hint);

        src.append(" ]");
    }
}
