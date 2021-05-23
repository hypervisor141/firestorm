package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLListType;

public abstract class FSGlobalData{

    protected VLListType<FSHAssembler> assemblers;
    protected VLListType<VLBuffer<?, ?>> buffers;
    protected VLListType<FSVertexBuffer<?>> vbuffers;
    protected VLListType<FSBufferTargets> targets;

    public FSGlobalData(){
        assemblers = generateAssemblers();
        buffers = generateBuffers();
        vbuffers = generateVertexBuffers();
        targets = generateBufferTargets();
    }

    protected abstract VLListType<FSHAssembler> generateAssemblers();
    protected abstract VLListType<VLBuffer<?, ?>> generateBuffers();
    protected abstract VLListType<FSVertexBuffer<?>> generateVertexBuffers();
    protected abstract VLListType<FSBufferTargets> generateBufferTargets();

    public FSHAssembler assembler(int index){
        return assemblers.get(index);
    }

    public VLBuffer<?, ?> buffer(int index){
        return buffers.get(index);
    }

    public FSVertexBuffer<?> vbuffer(int index){
        return vbuffers.get(index);
    }

    public FSBufferTargets bufferTarget(int index){
        return targets.get(index);
    }

    public VLListType<FSHAssembler> assemblers(){
        return assemblers;
    }

    public VLListType<VLBuffer<?, ?>> buffers(){
        return buffers;
    }

    public VLListType<FSVertexBuffer<?>> vbuffers(){
        return vbuffers;
    }

    public VLListType<FSBufferTargets> targets(){
        return targets;
    }

    public void releaseAssemblers(){
        assemblers = null;
    }

    public void releaseBufferTargets(){
        targets = null;
    }

    public void destroy(){
        int size = vbuffers.size();

        for(int i = 0; i < size; i++){
            vbuffers.get(i).destroy();
        }

        assemblers = null;
        buffers = null;
        vbuffers = null;
        targets = null;
    }
}
