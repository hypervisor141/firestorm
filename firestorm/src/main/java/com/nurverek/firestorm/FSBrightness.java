package com.nurverek.firestorm;

import vanguard.VLFloat;

public class FSBrightness{

    private VLFloat level;

    public FSBrightness(VLFloat level){
        this.level = level;
    }

    public void level(VLFloat level){
        this.level = level;
    }

    public VLFloat level(){
        return level;
    }
}
