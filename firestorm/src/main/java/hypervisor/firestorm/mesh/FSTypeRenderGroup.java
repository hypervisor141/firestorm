package hypervisor.firestorm.mesh;

import hypervisor.firestorm.automation.FSHScanner;
import hypervisor.vanguard.list.VLListType;

public interface FSTypeRenderGroup<ENTRY extends FSTypeRender> extends FSTypeRender{

    void register(FSHScanner<?> scanner);
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
