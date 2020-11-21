package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBufferManagerBase;
import com.nurverek.vanguard.VLBufferable;

public abstract class FSConfigLink<ENTRYTYPE extends VLBufferManagerBase.EntryType> extends FSConfig
        implements VLBufferable<ENTRYTYPE, FSBufferManager, FSBufferAddress>, FSBufferable<FSBufferAddress>{

    public int programid;
    public FSConfigLinkHost host;
    public FSBufferAddress address;

    public int indexonhost;

    public FSConfigLink(FSConfigLinkHost host, int indexonhost, int programid){
        this.host = host;
        this.indexonhost = indexonhost;
        this.programid = programid;

        address = new FSBufferAddress();
    }

    public FSConfigLink(){

    }

    @Override
    public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
        bind(address);
    }

    public FSConfigLink<ENTRYTYPE> attach(FSConfigLinkHost host, int programid){
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
