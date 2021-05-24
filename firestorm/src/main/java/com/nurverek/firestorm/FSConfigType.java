package com.nurverek.firestorm;

import vanguard.VLLog;

public interface FSConfigType{

    void run(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex);
    void runDebug(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex, VLLog log, int debug);
}
