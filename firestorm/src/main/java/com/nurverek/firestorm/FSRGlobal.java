package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLListType;

public abstract class FSRGlobal{

    protected VLListType<FSHAssembler> assemblers;
    protected VLListType<VLBuffer<?, ?>> buffers;
    protected VLListType<FSVertexBuffer<?>> vbuffers;
    protected VLListType<FSBufferTargets> targets;
    protected VLListType<FSP> programs;
    protected VLListType<FSRPass> passes;
    protected VLListType<FSHub> hubs;

    public FSRGlobal(){
        assemblers = generateAssemblers();
        buffers = generateBuffers();
        vbuffers = generateVertexBuffers();
        targets = generateBufferTargets();
        programs = generatePrograms();
        passes = generateRenderPasses();
        hubs = generateHubs();
    }

    protected abstract VLListType<FSHAssembler> generateAssemblers();
    protected abstract VLListType<VLBuffer<?, ?>> generateBuffers();
    protected abstract VLListType<FSVertexBuffer<?>> generateVertexBuffers();
    protected abstract VLListType<FSBufferTargets> generateBufferTargets();
    protected abstract VLListType<FSP> generatePrograms();
    protected abstract VLListType<FSRPass> generateRenderPasses();
    protected abstract VLListType<FSHub> generateHubs();

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

    public FSP program(int index){
        return programs.get(index);
    }

    public FSRPass pass(int index){
        return passes.get(index);
    }

    public FSHub hub(int index){
        return hubs.get(index);
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

    public VLListType<FSBufferTargets> bufferTargets(){
        return targets;
    }

    public VLListType<FSP> programs(){
        return programs;
    }

    public VLListType<FSRPass> passes(){
        return passes;
    }

    public VLListType<FSHub> hubs(){
        return hubs;
    }

    public void releaseAssemblers(){
        assemblers = null;
    }

    public void releaseBufferTargets(){
        targets = null;
    }

    void notifyFrameSwap(){
        int size = passes.size();

        for(int i = 0; i < size; i++){
            passes.get(i).noitifyPostFrameSwap();
        }
    }

    void paused(){
        int size = hubs.size();

        for(int i = 0; i < size; i++){
            hubs.get(i).paused();
        }
    }

    void resumed(){
        int size = hubs.size();

        for(int i = 0; i < size; i++){
            hubs.get(i).resumed();
        }
    }

    public void destroy(){
        int size = vbuffers.size();

        for(int i = 0; i < size; i++){
            vbuffers.get(i).destroy();
        }

        size = programs.size();

        for(int i = 0; i < size; i++){
            programs.get(i).destroy();
        }

        size = hubs.size();

        for(int i = 0; i < size; i++){
            hubs.get(i).destroy();
        }

        assemblers = null;
        buffers = null;
        vbuffers = null;
        targets = null;
        programs = null;
        passes = null;
        hubs = null;
    }
}
