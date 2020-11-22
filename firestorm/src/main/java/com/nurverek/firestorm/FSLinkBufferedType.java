package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBufferAddress;
import com.nurverek.vanguard.VLBufferManager;
import com.nurverek.vanguard.VLBufferManagerBase;
import com.nurverek.vanguard.VLBufferable;

public abstract class FSLinkBufferedType<DATA, MANAGER extends VLBufferManagerBase, ADDRESS extends VLBufferAddress<MANAGER>>
        extends FSLinkType<DATA> implements VLBufferable<MANAGER, ADDRESS>{

    public FSBufferAddress address;

    public FSLinkBufferedType(DATA data){
        super(data);
        address = new FSBufferAddress();
    }

    public FSLinkBufferedType(){

    }

    @Override
    public FSBufferAddress address(){
        return address;
    }

    public abstract int size();
}
