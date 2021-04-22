package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTrackerDetailed;

public abstract class FSLinkGLBuffered<DATA, BUFFER extends VLBuffer<?, ?>, TRACKER extends VLBufferTrackerDetailed> extends FSLinkBuffered<DATA, BUFFER, TRACKER>{

    public FSVertexBuffer<BUFFER> vbuffer;
    public int gldatatype;

    public FSLinkGLBuffered(DATA data, TRACKER tracker, FSVertexBuffer<BUFFER> vbuffer, int gldatatype){
        super(data, tracker, vbuffer.provider());
        this.vbuffer = vbuffer;
        this.gldatatype = gldatatype;
    }

    public FSLinkGLBuffered(DATA data, TRACKER tracker, int gldatatype){
        super(data, tracker);
        this.gldatatype = gldatatype;
    }

    @Override
    public void setVertexBuffer(FSVertexBuffer<BUFFER> buffer){
        this.vbuffer = buffer;
    }

    @Override
    public void updateBuffer(){
        super.updateBuffer();
        vbuffer.update();
    }
}
