package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLFloat;

public class FSBrightness implements VLCopyable<FSBrightness>{

    protected VLFloat level;

    public FSBrightness(VLFloat level){
        this.level = level;
    }

    public FSBrightness(FSBrightness src, long flags){
        copy(src, flags);
    }

    protected FSBrightness(){

    }

    public void level(VLFloat level){
        this.level = level;
    }

    public VLFloat level(){
        return level;
    }

    @Override
    public void copy(FSBrightness src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            level = src.level;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            level = src.level.duplicate(FLAG_DUPLICATE);

        }else{
            Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public FSBrightness duplicate(long flags){
        return new FSBrightness(this, flags);
    }
}
