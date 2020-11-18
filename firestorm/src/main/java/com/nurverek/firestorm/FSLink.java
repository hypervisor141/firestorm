package com.nurverek.firestorm;

public abstract class FSAttachment<ATTACHMENT, CONFIG extends FSConfig>{

    public ATTACHMENT attachment;
    public CONFIG config;
    public FSBufferAddress address;

    public FSAttachment(ATTACHMENT attachment, CONFIG config, FSBufferAddress address){
        this.attachment = attachment;
        this.config = config;
        this.address = address;
    }

    public FSAttachment(ATTACHMENT attachment, CONFIG config){
        this.attachment = attachment;
        this.config = config;
    }

    public abstract void buffer(FSBufferManager buffer, int index);
}
