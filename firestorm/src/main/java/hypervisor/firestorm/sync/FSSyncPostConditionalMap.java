package hypervisor.firestorm.sync;

import hypervisor.vanguard.sync.VLSyncType;

public abstract class FSSyncPostConditionalMap<SOURCE> extends FSSyncPostMap<SOURCE>{

    public FSSyncPostConditionalMap(VLSyncType<SOURCE> target){
        super(target);
    }

    public FSSyncPostConditionalMap(FSSyncPostConditionalMap<SOURCE> src, long flags){
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
