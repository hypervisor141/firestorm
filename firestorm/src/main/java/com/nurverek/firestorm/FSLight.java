package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLFloat;

public abstract class FSLight extends FSConfigSequence{

    protected long id;

    public FSLight(){
        id = FSControl.getNextID();
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
