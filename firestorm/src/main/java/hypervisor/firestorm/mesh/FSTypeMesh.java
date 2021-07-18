package hypervisor.firestorm.mesh;

import hypervisor.firestorm.automation.FSBufferMap;
import hypervisor.firestorm.automation.FSHAssembler;
import hypervisor.firestorm.automation.FSHScanner;
import hypervisor.firestorm.engine.FSGlobal;
import hypervisor.firestorm.engine.FSRPass;
import hypervisor.firestorm.program.FSP;
import hypervisor.vanguard.list.VLListType;

public interface FSTypeMesh<ENTRY extends FSTypeInstance> extends FSTypeRenderGroup<ENTRY>{

    FSHAssembler getAssembler(FSGlobal global);
    FSBufferMap getBufferMap(FSGlobal global);
    FSHScanner.ScanFunction getScanFunction(FSGlobal global);
    FSP getProgram(FSGlobal global);

    ENTRY generateInstance(String name);
    void addToDefinedProgram();
    void configure(FSP program, FSRPass pass, int targetindex, int passindex);
    void allocateBinding(int element, int capacity, int resizer);
    void bindManual(int element, FSBufferBinding<?> binding);
    FSBufferBinding<?> binding(int element, int index);
    void unbind(int element, int index);
    VLListType<FSBufferBinding<?>> bindings(int element);
    VLListType<FSBufferBinding<?>>[] bindings();
}
