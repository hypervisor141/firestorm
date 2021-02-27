package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;

public abstract class FSLight extends FSConfigSequence{

    protected long id;

    public FSLight(){
        id = FSRControl.getNextID();
    }

    public abstract String[] getStructMembers();

    public abstract String getLightFunction();

    public VLArrayFloat position(){
        return null;
    }

    public long id(){
        return id;
    }
}
