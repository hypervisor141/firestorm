package hypervisor.firestorm.sync;

import hypervisor.firestorm.engine.FSR;
import hypervisor.firestorm.engine.FSRTask;
import hypervisor.vanguard.sync.VLSyncType;
import hypervisor.vanguard.utils.VLCopyable;

public class FSSyncPostStickyMap<SOURCE> implements VLSyncType<SOURCE>{

    public FSRTask.SyncWrapper<SOURCE> task;

    public FSSyncPostStickyMap(VLSyncType<SOURCE> target){
        task = new FSRTask.SyncWrapper<>(target);
    }

    public FSSyncPostStickyMap(FSSyncPostStickyMap<SOURCE> src, long flags){
        copy(src, flags);
    }

    protected FSSyncPostStickyMap(){

    }

    @Override
    public void sync(SOURCE source){
        task.update(source);
        FSR.postStickyTask(task);
    }

    @Override
    public void copy(VLSyncType<SOURCE> src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            task = ((FSSyncPostStickyMap<SOURCE>)src).task;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            task = ((FSSyncPostStickyMap<SOURCE>)src).task.duplicate(VLCopyable.FLAG_DUPLICATE);

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    @Override
    public FSSyncPostStickyMap<SOURCE> duplicate(long flags){
        return new FSSyncPostStickyMap<>(this, flags);
    }
}
