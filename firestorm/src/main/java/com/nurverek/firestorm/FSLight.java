package com.nurverek.firestorm;

import vanguard.VLArrayFloat;

public abstract class FSLight{

    protected long id;

    public FSLight(){
        id = FSControl.getNextID();
    }

    public VLArrayFloat position(){
        return null;
    }

    public long id(){
        return id;
    }
}
