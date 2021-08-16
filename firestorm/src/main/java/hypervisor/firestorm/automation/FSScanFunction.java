package hypervisor.firestorm.automation;

import hypervisor.firestorm.io.FSM;
import hypervisor.firestorm.mesh.FSTypeInstance;
import hypervisor.firestorm.mesh.FSTypeMesh;

public interface FSScanFunction{

    boolean scan(FSTypeMesh<FSTypeInstance> target, FSHAssembler assembler, FSM.Data data);

    FSScanFunction SCAN_SINGULAR = (target, assembler, data) -> {
        if(target.size() <= 0 && data.name.startsWith(target.name())){
            FSTypeInstance instance = target.generateInstance(data.name);
            target.name(data.name);
            target.add(instance);

            assembler.buildFirst(instance, target, data);

            return true;
        }

        return false;
    };

    FSScanFunction SCAN_SINGULAR_STRICT = (target, assembler, data) -> {
        if(target.size() <= 0 && data.name.startsWith(target.name())){
            FSTypeInstance instance = target.generateInstance(data.name);
            target.name(data.name);
            target.add(instance);

            assembler.buildFirst(instance, target, data);
            data.locked = true;

            return true;
        }

        return false;
    };

    FSScanFunction SCAN_INSTANCED = (target, assembler, data) -> {
        if(data.name.startsWith(target.name())){
            FSTypeInstance instance = target.generateInstance(data.name);
            target.add(instance);

            if(target.size() == 1){
                assembler.buildFirst(instance, target, data);

            }else{
                assembler.buildRest(instance, target, data);
            }
        }

        return false;
    };
}
