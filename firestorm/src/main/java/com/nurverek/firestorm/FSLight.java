package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLCopyable;

public abstract class FSLight implements VLCopyable<FSLight>{

    public static final long FLAG_UNIQUE_ID = 0x10L;

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

    @Override
    public void copy(FSLight src, long flags){
        if((flags & FLAG_UNIQUE_ID) == FLAG_UNIQUE_ID){
            id = FSControl.getNextID();

        }else{
            id = src.id;
        }
    }

    @Override
    public abstract FSLight duplicate(long flags);
}
