package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLListType;

public abstract class FSBufferManager{

    protected VLListType<VLBuffer<?, ?>> buffers;
    protected VLListType<FSVertexBuffer<?>> vbuffers;
    protected VLListType<FSBufferTargets> targets;

    public FSBufferManager(int capacity, int resizer){
        targets = new VLListType<>(capacity, resizer);

        buffers = generateBuffers();
        vbuffers = generateVertexBuffers();
        targets = generateBufferTargets();
    }

    protected VLBuffer<?, ?> getBuffer(int index){
        return buffers.get(index);
    }

    protected FSVertexBuffer<?> getVBuffer(int index){
        return vbuffers.get(index);
    }

    protected FSBufferTargets getBufferTarget(int index){
        return targets.get(index);
    }

    protected abstract VLListType<VLBuffer<?, ?>> generateBuffers();
    protected abstract VLListType<FSVertexBuffer<?>> generateVertexBuffers();
    protected abstract VLListType<FSBufferTargets> generateBufferTargets();

    protected void destroy(){
        int size = vbuffers.size();

        for(int i = 0; i < size; i++){
            vbuffers.get(i).destroy();
        }

        buffers = null;
        vbuffers = null;
        targets = null;
    }
}
