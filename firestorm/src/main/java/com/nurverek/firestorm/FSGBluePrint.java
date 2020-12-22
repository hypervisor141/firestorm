package com.nurverek.firestorm;

public interface FSGBluePrint{

    FSGScanner register(FSG gen);
    void buffer(FSMesh mesh, FSBufferLayout layout, FSBufferManager manager);
    void makeLinks(FSMesh mesh);
    void program(FSMesh mesh);
}
