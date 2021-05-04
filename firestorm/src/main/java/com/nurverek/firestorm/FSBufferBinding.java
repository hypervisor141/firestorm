package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferFloat;
import vanguard.VLBufferTracker;
import vanguard.VLCopyable;

public class FSBufferBinding<BUFFER extends VLBuffer<?, ?>> implements VLCopyable<FSBufferBinding<BUFFER>>{

    public BUFFER buffer;
    public FSVertexBuffer<BUFFER> vbuffer;
    public VLBufferTracker tracker;

    public FSBufferBinding(FSVertexBuffer<BUFFER> vbuffer, BUFFER buffer, VLBufferTracker tracker){
        this.vbuffer = vbuffer;
        this.buffer = buffer;
        this.tracker = tracker;
    }

    public FSBufferBinding(FSBufferBinding<BUFFER> src, long flags){
        copy(src, flags);
    }

    public FSBufferBinding(){

    }

    public void updateVertexBuffer(){
        vbuffer.update();
    }

    public void updateVertexBufferStrict(){
        int offset = tracker.offset;
        vbuffer.update(offset,  tracker.endposition - offset);
    }

    @Override
    public void copy(FSBufferBinding<BUFFER> src, long flags){
        if((flags & FLAG_MINIMAL) == FLAG_MINIMAL){
            buffer = src.buffer;
            tracker = src.tracker;

        }else if((flags & FLAG_MAX_DEPTH) == FLAG_MAX_DEPTH){
            buffer = (BUFFER)src.buffer.duplicate(FLAG_MAX_DEPTH);
            tracker = src.tracker.duplicate(FLAG_MAX_DEPTH);

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }

        vbuffer = src.vbuffer;
    }

    @Override
    public FSBufferBinding<BUFFER> duplicate(long flags){
        return new FSBufferBinding<BUFFER>(this, flags);
    }
}
