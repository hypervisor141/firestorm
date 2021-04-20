package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTrackerDetailed;

public abstract class FSLinkGLBuffered<DATA, BUFFER extends VLBuffer<?, ?>, TRACKER extends VLBufferTrackerDetailed> extends FSLinkBuffered<DATA, BUFFER, TRACKER>{

    public FSVertexBuffer<BUFFER> vbuffer;

    public FSLinkGLBuffered(DATA data, TRACKER tracker, FSVertexBuffer<BUFFER> vbuffer){
        super(data, tracker, vbuffer.provider());
        this.vbuffer = vbuffer;
    }

    public FSLinkGLBuffered(DATA data, TRACKER tracker){
        super(data, tracker);
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
