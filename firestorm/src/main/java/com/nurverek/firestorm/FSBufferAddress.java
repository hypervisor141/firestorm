package com.nurverek.firestorm;

import android.opengl.GLES32;

import com.nurverek.vanguard.VLBufferAddress;
import com.nurverek.vanguard.VLBufferManagerBase;

public class FSBufferAddress extends VLBufferAddress<FSBufferManager>{

    public FSBufferAddress(FSBufferManager manager, int bufferindex, int offset, int unitoffset, int unitsize, int stride, int count){
        super(manager, bufferindex, offset, unitoffset, unitsize, stride, count);
    }

    public FSBufferAddress(){

    }

    @Override
    public FSEntryTypeVertexBuffer target(){
        return manager.get(bufferindex);
    }

    public void bind(){
        target().vertexBuffer().bind();
    }
    
    public void unbind(){
        target().vertexBuffer().unbind();
    }

    @Override
    public void stringify(StringBuilder src, Object hint){
        FSVertexBuffer buffer = target().vertexBuffer();

        src.append("[BufferAddress] managerType[");
        src.append(manager.getClass().getSimpleName());
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
