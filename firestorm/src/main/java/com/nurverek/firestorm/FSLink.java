package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBuffer;
import com.nurverek.vanguard.VLBufferable;

public class FSLink<LINK, BUFFERTYPE extends VLBuffer> implements VLBufferable<BUFFERTYPE>{

    public LINK link;
    public FSConfig config;
    public FSBufferAddress address;

    public FSLink(LINK link, FSConfig config){
        this.link = link;
        this.config = config;
    }

    public FSLink(LINK link){
        this.link = link;
    }

    @Override
    public void buffer(BUFFERTYPE buffertype){

    }

    @Override
    public void buffer(BUFFERTYPE buffertype, int i, int i1){

    }

    @Override
    public void buffer(BUFFERTYPE buffertype, int i, int i1, int i2, int i3, int i4, int i5){

    }
}
