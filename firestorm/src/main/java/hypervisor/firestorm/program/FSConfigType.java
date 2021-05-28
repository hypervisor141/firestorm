package hypervisor.firestorm.program;

import hypervisor.firestorm.engine.FSRPass;
import hypervisor.firestorm.mesh.FSInstance;
import hypervisor.firestorm.mesh.FSMesh;
import hypervisor.vanguard.utils.VLLog;

public interface FSConfigType{

    void run(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex);
    void runDebug(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex, VLLog log, int debug);
}
