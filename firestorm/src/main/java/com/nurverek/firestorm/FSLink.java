package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTracker;

public abstract class FSLinkType<DATA>{

    public DATA data;

    public FSLinkType(DATA data){
        this.data = data;
    }

    public FSLinkType(){

    }

    public abstract VLBufferTracker<VLBuffer<?, ?>> tracker();
}
