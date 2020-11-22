package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBufferManagerBase;
import com.nurverek.vanguard.VLBufferable;

public abstract class FSLinkType<DATA>{

    public DATA data;

    public FSLinkType(DATA data){
        this.data = data;
    }

    public FSLinkType(){

    }

    public abstract FSBufferAddress address();
}
