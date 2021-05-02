package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLListType;
import vanguard.VLLog;

public final class FSBufferMap{

    protected VLListType<FSBufferSegment<?>> segments;

    public FSBufferMap(){
        segments = new VLListType<>(FSGlobal.COUNT, FSGlobal.COUNT);
    }

    public <BUFFER extends VLBuffer<?, ?>> FSBufferSegment<BUFFER> add(FSBufferSegment<BUFFER> segment){
        segments.add(segment);
        return segment;
    }

    public void accountFor(FSMesh target){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            segments.get(i).accountFor(target);
        }
    }

    public void buffer(FSMesh target){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            segments.get(i).buffer(target);
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
                segments.get(i).bufferDebug(target, log);

            }catch(Exception ex){
                log.append(" [FAILED]\n");
                throw ex;
            }

            log.append(" [SUCCESS]\n");
        }

        log.append("\n");
    }
}
