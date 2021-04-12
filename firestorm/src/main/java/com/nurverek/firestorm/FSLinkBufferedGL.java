package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTracker;

public abstract class FSLinkBufferedGL<DATA, BUFFER extends VLBuffer<?, ?>, TRACKER extends VLBufferTracker> extends FSLinkBuffered<DATA, BUFFER, TRACKER>{

    public FSVertexBuffer<BUFFER> buffer;

    public FSLinkBufferedGL(DATA data, TRACKER tracker, FSVertexBuffer<BUFFER> buffer){
        super(data, tracker);
        this.buffer = buffer;
    }
}
