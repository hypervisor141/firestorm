package com.firestorm.engine;

import android.content.Context;

import com.firestorm.automation.FSBufferTargets;
import com.firestorm.automation.FSHAssembler;
import com.firestorm.program.FSP;
import com.firestorm.program.FSVertexBuffer;

import vanguard.buffer.VLBuffer;
import vanguard.concurrency.VLThread;
import vanguard.concurrency.VLThreadManager;
import vanguard.list.VLListType;

public abstract class FSGlobal{

    protected VLThreadManager threadmanager;
    protected VLListType<FSHAssembler> assemblers;
    protected VLListType<VLBuffer<?, ?>> buffers;
    protected VLListType<FSVertexBuffer<?>> vbuffers;
    protected VLListType<FSBufferTargets> targets;
    protected VLListType<FSP> programs;
    protected VLListType<FSRPass> passes;
    protected VLListType<FSHub> hubs;

    public FSGlobal(){

    }

    void initialize(Context context){
        threadmanager = generateThreads(context);
        assemblers = generateAssemblers(context);
        buffers = generateBuffers(context);
        vbuffers = generateVertexBuffers(context);
        targets = generateBufferTargets(context);
        programs = generatePrograms(context);
        passes = generateRenderPasses(context);
        hubs = generateHubs(context);

        buildPrograms();
        postSetup(context);
    }

    protected abstract VLThreadManager generateThreads(Context context);
    protected abstract VLListType<FSHAssembler> generateAssemblers(Context context);
    protected abstract VLListType<VLBuffer<?, ?>> generateBuffers(Context context);
    protected abstract VLListType<FSVertexBuffer<?>> generateVertexBuffers(Context context);
    protected abstract VLListType<FSBufferTargets> generateBufferTargets(Context context);
    protected abstract VLListType<FSP> generatePrograms(Context context);
    protected abstract VLListType<FSRPass> generateRenderPasses(Context context);
    protected abstract VLListType<FSHub> generateHubs(Context context);
    protected abstract void postSetup(Context context);
    protected abstract void paused();
    protected abstract void resumed();

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

    public VLThread worker(int index){
        return threadmanager.workers().get(index);
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

    public VLThreadManager threadManager(){
        return threadmanager;
    }

    public VLListType<VLThread> workers(){
        return threadmanager.workers();
    }

    public void releaseAssemblers(){
        assemblers = null;
    }

    public void releaseBufferTargets(){
        targets = null;
    }

    final void buildPrograms(){
        int size = programs.size();

        for(int i = 0; i < size; i++){
            programs.get(i).build();
        }
    }

    void notifyFrameSwap(){
        int size = passes.size();

        for(int i = 0; i < size; i++){
            passes.get(i).noitifyPostFrameSwap();
        }
    }

    void notifyPaused(){
        int size = hubs.size();

        for(int i = 0; i < size; i++){
            hubs.get(i).paused();
        }

        paused();
    }

    void notifyResumed(){
        int size = hubs.size();

        for(int i = 0; i < size; i++){
            hubs.get(i).resumed();
        }

        resumed();
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

        threadmanager.destroy();

        assemblers = null;
        buffers = null;
        vbuffers = null;
        targets = null;
        programs = null;
        passes = null;
        hubs = null;
        threadmanager = null;
    }
}
