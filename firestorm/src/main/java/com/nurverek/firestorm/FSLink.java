package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBufferAddress;
import com.nurverek.vanguard.VLBufferManagerBase;
import com.nurverek.vanguard.VLBufferable;

public abstract class FSLink<CONFIG extends FSConfig, ENTRYTYPE extends VLBufferManagerBase.EntryType,
        MANAGERTYPE extends VLBufferManagerBase, ADDRESSTYPE extends VLBufferAddress<MANAGERTYPE>>
        implements VLBufferable<ENTRYTYPE, MANAGERTYPE, ADDRESSTYPE>{

    public CONFIG config;
    public int programid;
    public FSConfigLinks host;
    public FSBufferAddress address;

    public int indexonhost;

    public FSLink(CONFIG config, FSConfigLinks host, int indexonhost, int programid){
        this.config = config;
        this.host = host;
        this.indexonhost = indexonhost;
        this.programid = programid;
    }

    public FSLink(){

    }

    public FSLink<CONFIG, ENTRYTYPE, MANAGERTYPE, ADDRESSTYPE> attach(FSConfigLinks host, int programid){
        this.programid = programid;

        indexonhost = host.links().size();
        host.links().add(this);

        return this;
    }

    public void activate(int programID){
        if(programID == programid){
            host.activate(indexonhost);
        }
    }

    public abstract int size();
}
