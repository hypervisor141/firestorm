package com.nurverek.firestorm;

public abstract class FSGBluePrint{

    protected FSGBluePrint(){

    }

    protected abstract FSGScanner createScanner(String name);
    protected abstract void foundNewInstance(FSMesh mesh, FSInstance instance);
    protected abstract void builtNewInstance(FSMesh mesh, FSInstance instance);
    protected abstract void meshComplete(FSMesh mesh);
    protected abstract FSBufferLayout buildBufferLayouts(FSMesh mesh);
    protected abstract void finished(FSMesh mesh);
}
