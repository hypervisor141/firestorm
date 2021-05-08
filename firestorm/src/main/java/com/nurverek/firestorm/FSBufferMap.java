package com.nurverek.firestorm;

import java.nio.ByteOrder;

import vanguard.VLBuffer;
import vanguard.VLListType;
import vanguard.VLLog;

public final class FSBufferMap<BUFFER extends VLBuffer<?, ?>>{

    protected VLListType<FSBufferSegment<BUFFER>> segments;
    protected FSVertexBuffer<BUFFER> vbuffer;
    protected BUFFER buffer;

    private boolean initialized;
    private boolean uploaded;

    public FSBufferMap(FSVertexBuffer<BUFFER> vbuffer, int capacity){
        this.vbuffer = vbuffer;
        this.buffer = vbuffer.provider();

        segments = new VLListType<>(capacity, capacity);

        initialized = false;
        uploaded = false;
    }

    public FSBufferMap(BUFFER buffer, int capacity){
        this.buffer = vbuffer.provider();
        segments = new VLListType<>(capacity, capacity);

        initialized = false;
        uploaded = false;
    }

    public FSBufferSegment<BUFFER> add(FSBufferSegment<BUFFER> segment){
        segments.add(segment);
        return segment;
    }

    public void accountFor(FSMesh target){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            segments.get(i).accountFor(target, buffer);
        }
    }

    public void initialize(){
        if(!initialized){
            buffer.initialize(ByteOrder.nativeOrder());

            if(vbuffer != null){
                vbuffer.initialize();
            }

            initialized = true;
        }
    }

    public void upload(){
        if(!uploaded && vbuffer != null){
            vbuffer.upload();
            uploaded = true;
        }
    }

    public void buffer(FSMesh target){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            segments.get(i).buffer(target, buffer, vbuffer);
        }
    }

    public void bufferDebug(FSMesh target, VLLog log){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            log.append("Segment[");
            log.append(i);
            log.append("/");
            log.append(size);
            log.append("] ");
            log.printInfo();

            try{
                segments.get(i).bufferDebug(target, buffer, vbuffer, log);

            }catch(Exception ex){
                log.append(" [FAILED]\n");
                throw ex;
            }

            log.append(" [SUCCESS]\n");
        }

        log.append("\n");
    }
}
