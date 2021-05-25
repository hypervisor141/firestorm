package com.firestorm.program;

import com.firestorm.engine.FSControl;

import vanguard.array.VLArrayFloat;
import vanguard.utils.VLCopyable;

public abstract class FSLight implements VLCopyable<FSLight>{

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
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            id = src.id;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            id = FSControl.getNextID();

        }else{
            Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public abstract FSLight duplicate(long flags);
}
