package hypervisor.firestorm.engine;

import android.content.Context;

import hypervisor.firestorm.automation.FSBufferMap;
import hypervisor.firestorm.automation.FSHAssembler;
import hypervisor.firestorm.program.FSP;
import hypervisor.firestorm.program.FSVertexBuffer;
import hypervisor.vanguard.buffer.VLBuffer;
import hypervisor.vanguard.concurrency.VLThread;
import hypervisor.vanguard.concurrency.VLThreadManager;
import hypervisor.vanguard.list.VLListType;

public abstract class FSGlobal{

    private static FSGlobal GLOBAL;

    protected VLThreadManager threadmanager;
    protected VLListType<FSHAssembler> assemblers;
    protected VLListType<VLBuffer<?, ?>> buffers;
    protected VLListType<FSVertexBuffer<?>> vbuffers;
    protected VLListType<FSBufferMap> buffermaps;
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
        buffermaps = generateBufferMaps(context);
        programs = generatePrograms(context);
        passes = generateRenderPasses(context);
        hubs = generateHubs(context);

        buildPrograms();
        postSetup(context);
    }

    public static void initialize(FSGlobal global){
        GLOBAL = global;
    }

    public static FSGlobal get(){
        return GLOBAL;
    }

    protected abstract VLThreadManager generateThreads(Context context);
    protected abstract VLListType<FSHAssembler> generateAssemblers(Context context);
    protected abstract VLListType<VLBuffer<?, ?>> generateBuffers(Context context);
    protected abstract VLListType<FSVertexBuffer<?>> generateVertexBuffers(Context context);
    protected abstract VLListType<FSBufferMap> generateBufferMaps(Context context);
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

    public FSBufferMap bufferMap(int index){
        return buffermaps.get(index);
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

    public VLListType<FSBufferMap> bufferMaps(){
        return buffermaps;
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

    public void releaseBufferMaps(){
        buffermaps = null;
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
        buffermaps = null;
        programs = null;
        passes = null;
        hubs = null;
        threadmanager = null;
    }

    protected static void destroy(boolean destroyonpause){
        if(destroyonpause){
            GLOBAL = null;
        }
    }
}
