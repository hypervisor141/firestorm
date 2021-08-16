package hypervisor.firestorm.mesh;

import hypervisor.firestorm.automation.FSScanTarget;
import hypervisor.firestorm.tools.FSLog;
import hypervisor.vanguard.list.arraybacked.VLListType;

public interface FSTypeRenderGroup<ENTRY extends FSTypeRender> extends FSTypeRender{

    void autoScan(FSScanTarget target);
    void autoAccountForBufferCapacity();
    void autoAccountForBufferCapacityDebug(FSLog log);
    void autoBuildBuffer();
    void autoBuildBufferDebug(FSLog log);
    void autoUploadBuffer();
    void autoBuild();
    void autoBuildDebug(FSLog log);
    void autoScanBuild(FSScanTarget target);
    void autoScanBuildDebug(FSScanTarget target, FSLog log);
    void registerWithPrograms();
    void unregisterFromProgram();
    void add(ENTRY entry);
    void remove(int index);
    void remove(ENTRY entry);
    void enable(boolean enabled);
    ENTRY first();
    ENTRY last();
    ENTRY get(int index);
    VLListType<ENTRY> get();
    boolean enabled();
    int size();
}
