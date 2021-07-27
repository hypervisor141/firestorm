package hypervisor.firestorm.sync;

import hypervisor.vanguard.sync.VLSyncType;

public abstract class FSSyncRemoveStickyConditionalMap<SOURCE> extends FSSyncRemoveStickyMap<SOURCE>{

    public FSSyncRemoveStickyConditionalMap(VLSyncType<SOURCE> target){
        super(target);
    }

    public FSSyncRemoveStickyConditionalMap(FSSyncRemoveStickyConditionalMap<SOURCE> src, long flags){
        copy(src, flags);
    }

    protected FSSyncRemoveStickyConditionalMap(){

    }

    protected abstract boolean checkCondition();

    @Override
    public void sync(SOURCE source){
        if(checkCondition()){
            super.sync(source);
        }
    }
}
