package hypervisor.firestorm.sync;

import hypervisor.firestorm.engine.FSR;
import hypervisor.firestorm.engine.FSRTask;
import hypervisor.vanguard.sync.VLSyncType;
import hypervisor.vanguard.utils.VLCopyable;

public class FSSyncPostMap<SOURCE> implements VLSyncType<SOURCE>{

    public FSRTask.SyncWrapper<SOURCE> task;

    public FSSyncPostMap(VLSyncType<SOURCE> target){
        task = new FSRTask.SyncWrapper<>(target);
    }

    public FSSyncPostMap(FSSyncPostMap<SOURCE> src, long flags){
        copy(src, flags);
    }

    protected FSSyncPostMap(){

    }

    @Override
    public void sync(SOURCE source){
        task.update(source);
        FSR.postTask(task);
    }

    @Override
    public void copy(VLSyncType<SOURCE> src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            task = ((FSSyncPostMap<SOURCE>)src).task;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            task = ((FSSyncPostMap<SOURCE>)src).task.duplicate(VLCopyable.FLAG_DUPLICATE);

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    @Override
    public FSSyncPostMap<SOURCE> duplicate(long flags){
        return new FSSyncPostMap<>(this, flags);
    }
}
