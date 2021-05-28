package hypervisor.firestorm.mesh;

import hypervisor.firestorm.program.FSVertexBuffer;
import hypervisor.vanguard.buffer.VLBuffer;
import hypervisor.vanguard.buffer.VLBufferTracker;
import hypervisor.vanguard.utils.VLCopyable;

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

    protected FSBufferBinding(){

    }

    public void updateVertexBuffer(){
        vbuffer.update();
    }

    public void updateVertexBufferStrict(){
        int offset = tracker.offset;
        int count = tracker.endposition - offset;

        if(count != 0){
            vbuffer.update(offset,  count);
        }
    }

    @Override
    public void copy(FSBufferBinding<BUFFER> src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            buffer = src.buffer;
            tracker = src.tracker;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            VLCopyable.Helper.throwUnsupportedFlag("FLAG_DUPLICATE");

        }else{
            VLCopyable.Helper.throwMissingDefaultFlags();
        }

        vbuffer = src.vbuffer;
    }

    @Override
    public FSBufferBinding<BUFFER> duplicate(long flags){
        return new FSBufferBinding<BUFFER>(this, flags);
    }
}
