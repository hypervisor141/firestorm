package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBuffer;
import com.nurverek.vanguard.VLBufferAddress;
import com.nurverek.vanguard.VLBufferManagerBase;
import com.nurverek.vanguard.VLBufferable;

public abstract class FSLink<LINK, CONFIG extends FSConfig, ENTRYTYPE extends VLBufferManagerBase.EntryType,
        MANAGERTYPE extends VLBufferManagerBase, ADDRESSTYPE extends VLBufferAddress<MANAGERTYPE>>
        implements VLBufferable<ENTRYTYPE, MANAGERTYPE, ADDRESSTYPE>{

    public LINK link;
    public CONFIG config;
    public FSBufferAddress address;

    public FSLink(LINK link, CONFIG config){
        this.link = link;
        this.config = config;
    }

    public FSLink(LINK link){
        this.link = link;
    }

    public abstract int size();
}
