package hypervisor.firestorm.sync;

import hypervisor.vanguard.sync.VLSyncType;

public abstract class FSSyncPostStickyConditionalMap<SOURCE> extends FSSyncPostStickyMap<SOURCE>{

    public FSSyncPostStickyConditionalMap(VLSyncType<SOURCE> target){
        super(target);
    }

    public FSSyncPostStickyConditionalMap(FSSyncPostStickyConditionalMap<SOURCE> src, long flags){
        copy(src, flags);
    }

    protected FSSyncPostStickyConditionalMap(){

    }

    protected abstract boolean checkCondition();

    @Override
    public void sync(SOURCE source){
        if(checkCondition()){
            super.sync(source);
        }
    }
}
