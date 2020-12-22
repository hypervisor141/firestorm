package com.nurverek.firestorm;

public abstract class FSGBluePrint{

    protected FSMesh mesh;

    protected FSGBluePrint(){

    }

    public FSMesh mesh(){
        return mesh;
    }

    protected abstract FSGScanner register(FSG gen);
    protected abstract void adjustPreAssembly(FSMesh mesh, FSInstance instance);
    protected abstract void adjustPostScan(FSMesh mesh);
    protected abstract void makeLinks(FSMesh mesh);
    protected abstract FSBufferLayout layout(FSMesh mesh, FSBufferManager manager);
    protected abstract void adjustPostBuffer(FSMesh mesh);
    protected abstract void program(FSG gen, FSMesh mesh);
}
