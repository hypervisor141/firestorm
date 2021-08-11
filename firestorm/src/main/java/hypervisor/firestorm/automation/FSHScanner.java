package hypervisor.firestorm.automation;

import hypervisor.firestorm.engine.FSGlobal;
import hypervisor.firestorm.io.FSM;
import hypervisor.firestorm.mesh.FSTypeInstance;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.firestorm.mesh.FSTypeRenderGroup;
import hypervisor.vanguard.list.arraybacked.VLListType;
import hypervisor.vanguard.utils.VLLog;

public class FSHScanner<TYPE extends FSTypeRenderGroup<?>>{

    protected TYPE target;
    protected VLListType<FSTypeMesh<FSTypeInstance>> targets;

    public FSHScanner(TYPE target, int capacity){
        this.target = target;
        targets = new VLListType<>(capacity, capacity);

        target.register(this);
    }

    protected FSHScanner(){

    }

    public void register(FSTypeMesh<FSTypeInstance> target){
        targets.add(target);
    }

    protected void scan(FSM.Data data){
        int size = targets.size();
        FSGlobal global = FSGlobal.get();

        for(int i = 0; i < size; i++){
            FSTypeMesh<FSTypeInstance> target = targets.get(i);
            target.getScanFunction(global).scan(target, target.getAssembler(global), data);

            if(data.locked){
                return;
            }
        }
    }

    public void checkResults(VLLog log){
        int size = targets.size();

        for(int i = 0; i < size; i++){
            FSTypeMesh<FSTypeInstance> target = targets.get(i);

            if(target.size() == 0){
                log.append("Incomplete scan : found no instance for mesh target with keyword[");
                log.append(target.name());
                log.append("] at [");
                log.append(i);
                log.append("/");
                log.append(size);
                log.append("]");

                log.printError();

                throw new RuntimeException();
            }
        }
    }

    public void signalScanComplete(){
        target.scanComplete();
    }

    public void signalBufferComplete(){
        target.bufferComplete();
    }

    public void finalizeBuild(){
        int size = targets.size();

        for(int i = 0; i < size; i++){
            targets.get(i).addToDefinedProgram();
        }

        target.buildComplete();
    }

    public void accountForTargetSize(){
        int size = targets.size();
        FSGlobal global = FSGlobal.get();

        for(int i = 0; i < size; i++){
            FSBufferMap map = targets.get(i).getBufferMap(global);

            if(map != null){
                map.accountFor(targets.get(i));
            }
        }
    }

    public void accountForTargetSizeDebug(VLLog log){
        int size = targets.size();
        FSGlobal global = FSGlobal.get();

        for(int i = 0; i < size; i++){
            FSBufferMap map = targets.get(i).getBufferMap(global);

            if(map != null){
                map.accountForDebug(targets.get(i), log);
            }
        }
    }

    public void buffer(){
        int size = targets.size();
        FSGlobal global = FSGlobal.get();

        for(int i = 0; i < size; i++){
            FSBufferMap map = targets.get(i).getBufferMap(global);

            if(map != null){
                map.buffer(targets.get(i));
            }
        }
    }

    public void bufferDebug(VLLog log){
        int size = targets.size();
        FSGlobal global = FSGlobal.get();

        if(size > 0){
            for(int i = 0; i < size; i++){
                FSBufferMap map = targets.get(i).getBufferMap(global);

                if(map != null){
                    map.bufferDebug(targets.get(i), log);

                }else{
                    log.append("[Buffering disabled for submesh] [");
                    log.append(targets.get(i).name());
                    log.append("] ");
                }
            }

        }else{
            log.append("[Buffering disabled] ");
        }
    }

    public void uploadBuffer(){
        int size = targets.size();
        FSGlobal global = FSGlobal.get();

        for(int i = 0; i < size; i++){
            FSBufferMap map = targets.get(i).getBufferMap(global);

            if(map != null){
                map.upload();
            }
        }
    }

    public interface ScanFunction{

        void scan(FSTypeMesh<FSTypeInstance> target, FSHAssembler assembler, FSM.Data data);

        ScanFunction SCAN_SINGULAR = (target, assembler, data) -> {
            if(target.size() <= 0 && data.name.startsWith(target.name())){
                FSTypeInstance instance = target.generateInstance(data.name);
                target.name(data.name);
                target.add(instance);

                assembler.buildFirst(instance, target, data);
            }
        };

        ScanFunction SCAN_SINGULAR_STRICT = (target, assembler, data) -> {
            if(target.size() <= 0 && data.name.startsWith(target.name())){
                FSTypeInstance instance = target.generateInstance(data.name);
                target.name(data.name);
                target.add(instance);

                assembler.buildFirst(instance, target, data);
                data.locked = true;
            }
        };

        ScanFunction SCAN_INSTANCED = (target, assembler, data) -> {
            if(data.name.startsWith(target.name())){
                FSTypeInstance instance = target.generateInstance(data.name);
                target.add(instance);

                if(target.size() == 1){
                    assembler.buildFirst(instance, target, data);

                }else{
                    assembler.buildRest(instance, target, data);
                }
            }
        };
    }
}
