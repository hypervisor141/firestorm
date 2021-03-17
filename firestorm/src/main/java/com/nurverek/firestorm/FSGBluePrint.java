package com.nurverek.firestorm;

public abstract class FSGBluePrint{

    protected FSGBluePrint(){

    }

    public final void initialize(FSG<?> gen, int passindex){
        attachPrograms(gen, passindex);
    }

    protected abstract void attachPrograms(FSG<?> gen, int passindex);
    protected abstract FSGScanner register(FSG<?> gen, String name);
    protected abstract void preAssemblyAdjustment(FSMesh mesh, FSInstance instance);
    protected abstract void postScanAdjustment(FSMesh mesh);
    protected abstract void createLinks(FSMesh mesh);
    protected abstract FSBufferLayout bufferLayouts(FSMesh mesh);
    protected abstract void postBufferAdjustment(FSMesh mesh);
    protected abstract void attachMeshToPrograms(FSMesh mesh);
    protected abstract void finished(FSMesh mesh);
}
