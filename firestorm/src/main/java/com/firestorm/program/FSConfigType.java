package com.firestorm.program;

import com.firestorm.engine.FSRPass;
import com.firestorm.mesh.FSInstance;
import com.firestorm.mesh.FSMesh;

import vanguard.utils.VLLog;

public interface FSConfigType{

    void run(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex);
    void runDebug(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex, VLLog log, int debug);
}
