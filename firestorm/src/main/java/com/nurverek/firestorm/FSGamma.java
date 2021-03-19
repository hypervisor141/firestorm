package com.nurverek.firestorm;

import vanguard.VLFloat;

public class FSGamma{

    private VLFloat level;

    public FSGamma(VLFloat level){
        this.level = level;
    }

    public void level(VLFloat level){
        this.level = level;
    }

    public VLFloat level(){
        return level;
    }
}

