package com.nurverek.firestorm;

import vanguard.VLBuffer;

public abstract class FSLinkGLBuffered<DATA, BUFFER extends VLBuffer<?, ?>> extends FSLinkBuffered<DATA, BUFFER>{

    public FSVertexBuffer<BUFFER> vbuffer;
    public int gldatatype;

    public FSLinkGLBuffered(DATA data, FSVertexBuffer<BUFFER> vbuffer, int gldatatype){
        super(data, vbuffer.provider());
        this.vbuffer = vbuffer;
        this.gldatatype = gldatatype;
    }

    public FSLinkGLBuffered(DATA data, int gldatatype){
        super(data);
        this.gldatatype = gldatatype;
    }

    @Override
    public void setVertexBuffer(FSVertexBuffer<BUFFER> buffer){
        this.vbuffer = buffer;
    }

    public void updateVertexBuffer(){
        vbuffer.update();
    }

    public void updateVertexBufferStrict(){
        int offset = tracker.offset;
        vbuffer.update(offset, tracker.endposition - offset);
    }
}
