package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTrackerDetailed;

public abstract class FSLinkBufferedGL<DATA, BUFFER extends VLBuffer<?, ?>, TRACKER extends VLBufferTrackerDetailed> extends FSLinkBuffered<DATA, BUFFER, TRACKER>{

    public FSVertexBuffer<BUFFER> vbuffer;

    public FSLinkBufferedGL(DATA data, TRACKER tracker, FSVertexBuffer<BUFFER> vbuffer){
        super(data, tracker, vbuffer.provider());
        this.vbuffer = vbuffer;
    }

    @Override
    public void updateBuffer(){
        super.updateBuffer();
        vbuffer.update();
    }
}
