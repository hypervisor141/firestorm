package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBufferAddress;
import com.nurverek.vanguard.VLBufferManagerBase;
import com.nurverek.vanguard.VLBufferable;

public abstract class FSLink<CONFIG extends FSConfig, ENTRYTYPE extends VLBufferManagerBase.EntryType,
        MANAGERTYPE extends VLBufferManagerBase, ADDRESSTYPE extends VLBufferAddress<MANAGERTYPE>>
        implements VLBufferable<ENTRYTYPE, MANAGERTYPE, ADDRESSTYPE>{

    public CONFIG config;
    public FSConfigDynamic<CONFIG> host;
    public FSBufferAddress address;

    public FSLink(CONFIG config, FSConfigDynamic host){
        this.config = config;
        this.host = host;
    }

    public FSLink(){

    }

    public void attach(){
        host.config(config);
    }

    public abstract int size();
}
