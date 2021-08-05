package hypervisor.firestorm.sync;

import hypervisor.firestorm.engine.FSRTask;
import hypervisor.vanguard.sync.VLSyncType;
import hypervisor.vanguard.utils.VLCopyable;

public class FSRTaskSyncWrapper<SOURCE> implements FSRTask, VLCopyable<FSRTaskSyncWrapper<SOURCE>>{

    public VLSyncType<SOURCE> target;
    public SOURCE source;

    public FSRTaskSyncWrapper(VLSyncType<SOURCE> target){
        this.target = target;
    }

    public FSRTaskSyncWrapper(FSRTaskSyncWrapper<SOURCE> src, long flags){
        copy(src, flags);
    }

    protected FSRTaskSyncWrapper(){

    }

    public void update(SOURCE source){
        this.source = source;
    }

    @Override
    public void run(){
        target.sync(source);
    }

    @Override
    public void copy(FSRTaskSyncWrapper<SOURCE> src, long flags){
        source = src.source;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            this.target = src.target;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            this.target = src.target.duplicate(FLAG_DUPLICATE);

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    @Override
    public FSRTaskSyncWrapper<SOURCE> duplicate(long flags){
        return new FSRTaskSyncWrapper<>(this, flags);
    }
}
