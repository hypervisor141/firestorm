package hypervisor.firestorm.sync;

import hypervisor.vanguard.sync.VLSyncType;

public abstract class FSSyncPostConditionalMap<SOURCE, TARGET extends VLSyncType<SOURCE>> extends FSSyncPostMap<SOURCE, TARGET>{

    public FSSyncPostConditionalMap(TARGET target){
        super(target);
    }

    public FSSyncPostConditionalMap(FSSyncPostConditionalMap<SOURCE, TARGET> src, long flags){
        copy(src, flags);
    }

    protected FSSyncPostConditionalMap(){

    }

    protected abstract boolean checkCondition();

    @Override
    public void sync(SOURCE source){
        if(checkCondition()){
            super.sync(source);
        }
    }
}
