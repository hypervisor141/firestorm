package com.nurverek.firestorm;

import android.opengl.GLES32;

import com.nurverek.vanguard.VLBufferAddress;
import com.nurverek.vanguard.VLBufferManagerBase;

public class FSBufferAddress extends VLBufferAddress<FSBufferManager> {

    public FSBufferAddress(FSBufferManager manager, int bufferindex, int offset, int unitoffset, int unitsize, int stride, int count){
        super(manager, bufferindex, offset, unitoffset, unitsize, stride, count);
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
}
