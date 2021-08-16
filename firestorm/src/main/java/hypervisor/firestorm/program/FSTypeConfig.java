package hypervisor.firestorm.program;

import hypervisor.firestorm.engine.FSRPass;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.firestorm.tools.FSLog;

public interface FSTypeConfig{

    void run(FSRPass pass, FSP program, FSTypeMesh<?> mesh, int meshindex, int passindex);
    void runDebug(FSRPass pass, FSP program, FSTypeMesh<?> mesh, int meshindex, int passindex, FSLog log, int debug);
}
