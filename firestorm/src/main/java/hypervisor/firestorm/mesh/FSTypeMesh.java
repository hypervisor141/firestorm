package hypervisor.firestorm.mesh;

import hypervisor.firestorm.automation.FSBufferMap;
import hypervisor.firestorm.automation.FSHAssembler;
import hypervisor.firestorm.automation.FSScanFunction;
import hypervisor.firestorm.engine.FSRPass;
import hypervisor.firestorm.program.FSP;
import hypervisor.vanguard.list.arraybacked.VLListType;

public interface FSTypeMesh<ENTRY extends FSTypeInstance> extends FSTypeRenderGroup<ENTRY>{

    FSHAssembler assembler();
    FSBufferMap bufferMap();
    FSScanFunction scanFunction();
    FSP program();

    ENTRY generateInstance(String name);
    void configure(FSP program, FSRPass pass, int targetindex, int passindex);
    void allocateBinding(int element, int capacity, int resizeoverhead);
    void bindManual(int element, FSBufferBinding<?> binding);
    FSBufferBinding<?> binding(int element, int index);
    void unbind(int element, int index);
    VLListType<FSBufferBinding<?>> bindings(int element);
    VLListType<FSBufferBinding<?>>[] bindings();
}
