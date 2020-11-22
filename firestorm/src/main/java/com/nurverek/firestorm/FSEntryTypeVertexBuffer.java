package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArray;
import com.nurverek.vanguard.VLBuffer;
import com.nurverek.vanguard.VLBufferManagerBase;

public abstract class FSEntryTypeVertexBuffer<TYPE extends VLArray> extends VLBufferManagerBase.EntryType<TYPE>{

    protected FSVertexBuffer vertexbuffer;

    protected FSEntryTypeVertexBuffer(FSVertexBuffer vbuffer, VLBuffer buffer){
        super(buffer);
        vbuffer.provider(buffer);
        this.vertexbuffer = vbuffer;
    }

    @Override
    protected void initialize(){
        super.initialize();
        vertexbuffer.initialize();
    }

    public FSVertexBuffer vertexBuffer(){
        return vertexbuffer;
    }
}
