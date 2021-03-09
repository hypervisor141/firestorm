package com.nurverek.firestorm;

import vanguard.VLBufferAddress;
import vanguard.VLBufferManagerBase;
import vanguard.VLBufferTrackerType;
import vanguard.VLBufferable;

public abstract class FSLinkBufferedType<DATA, MANAGER extends VLBufferManagerBase, ADDRESS extends VLBufferAddress<MANAGER>>
        extends FSLinkType<DATA> implements VLBufferable<MANAGER, ADDRESS>{

    public VLBufferTrackerType address;

    public FSLinkBufferedType(DATA data, VLBufferTrackerType address){
        super(data);
        this.address = address;
    }

    public FSLinkBufferedType(){

    }

    @Override
    public VLBufferTrackerType address(){
        return address;
    }

    public abstract int size();
}
