package com.nurverek.firestorm;

public abstract class FSLinkType<DATA>{

    public DATA data;

    public FSLinkType(DATA data){
        this.data = data;
    }

    public FSLinkType(){

    }

    public abstract FSBufferAddress address();
}
