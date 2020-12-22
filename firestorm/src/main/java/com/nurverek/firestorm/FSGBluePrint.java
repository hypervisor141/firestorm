package com.nurverek.firestorm;

public interface FSGBluePrint{

    FSGScanner register(FSG gen);
    FSBufferLayout layout(FSMesh mesh, FSBufferManager manager);
    void makeLinks(FSMesh mesh);
    void program(FSG gen, FSMesh mesh);
}
