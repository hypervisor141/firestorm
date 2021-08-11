package hypervisor.firestorm.automation;

import hypervisor.firestorm.mesh.FSTypeInstance;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.vanguard.list.arraybacked.VLListType;
import hypervisor.vanguard.utils.VLLog;

public class FSBufferMap{

    protected VLListType<FSBufferSegment<?>> map;

    public FSBufferMap(int capacity){
        map = new VLListType<>(capacity, capacity);
    }

    protected FSBufferMap(){

    }

    public FSBufferMap add(FSBufferSegment<?> segment){
        map.add(segment);
        return this;
    }

    public void accountFor(FSTypeMesh<FSTypeInstance> target){
        int size = map.size();
        
        for(int i = 0; i < size; i++){
            map.get(i).accountFor(target);
        }
    }

    public void accountForDebug(FSTypeMesh<FSTypeInstance> target, VLLog log){
        int size = map.size();
        log.addTag(getClass().getSimpleName());

        for(int i = 0; i < size; i++){
            log.addTag(String.valueOf(i));

            map.get(i).accountForDebug(target, log);

            log.removeLastTag();
        }

        log.removeLastTag();
    }

    public void buffer(FSTypeMesh<FSTypeInstance> target){
        int size = map.size();

        for(int i = 0; i < size; i++){
            map.get(i).buffer(target);
        }
    }

    public void bufferDebug(FSTypeMesh<FSTypeInstance> target, VLLog log){
        int size = map.size();
        log.addTag(getClass().getSimpleName());

        for(int i = 0; i < size; i++){
            log.addTag(String.valueOf(i));

            map.get(i).bufferDebug(target, log);

            log.removeLastTag();
        }

        log.removeLastTag();
    }

    public void upload(){
        int size = map.size();

        for(int i = 0; i < size; i++){
            map.get(i).upload();
        }
    }

    public VLListType<FSBufferSegment<?>> get(){
        return map;
    }

    public int size(){
        return map.size();
    }
}
