package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLFloat;

public class FSBrightness implements VLCopyable<FSBrightness>{

    private VLFloat level;

    public FSBrightness(VLFloat level){
        this.level = level;
    }

    public FSBrightness(FSBrightness src, long flags){
        copy(src, flags);
    }

    public void level(VLFloat level){
        this.level = level;
    }

    public VLFloat level(){
        return level;
    }

    @Override
    public void copy(FSBrightness src, long flags){
        if((flags & FLAG_MINIMAL) == FLAG_MINIMAL){
            level = src.level;

        }else if((flags & FLAG_MAX_DEPTH) == FLAG_MAX_DEPTH){
            level = src.level.duplicate(FLAG_MAX_DEPTH);

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }
    }

    @Override
    public FSBrightness duplicate(long flags){
        return new FSBrightness(this, flags);
    }
}
