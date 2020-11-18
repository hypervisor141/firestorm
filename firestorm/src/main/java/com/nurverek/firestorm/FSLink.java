package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBuffer;
import com.nurverek.vanguard.VLBufferable;

public abstract class FSLink<LINK, BUFFERTYPE extends VLBuffer, CONFIG extends FSConfig> implements VLBufferable<BUFFERTYPE>{

    public LINK link;
    public CONFIG config;
    public FSBufferAddress address;

    public FSLink(LINK link, CONFIG config){
        this.link = link;
        this.config = config;
    }

    public FSLink(LINK link){
        this.link = link;
    }
}
