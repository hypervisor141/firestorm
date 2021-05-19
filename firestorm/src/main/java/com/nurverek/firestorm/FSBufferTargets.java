package com.nurverek.firestorm;

import vanguard.VLListType;
import vanguard.VLLog;

public class FSBufferTargets{

    protected VLListType<FSBufferSegment<?>> segments;

    public FSBufferTargets(int capacity){
        segments = new VLListType<>(capacity, capacity);
    }

    public FSBufferTargets add(FSBufferSegment<?> segment){
        segments.add(segment);
        return this;
    }

    public void prepare(FSMesh target){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            segments.get(i).prepare(target);
        }
    }

    public void prepareDebug(FSMesh target, VLLog log){
        int size = segments.size();

        log.append("[");
        log.append(getClass().getSimpleName());
        log.append("]\n");
        log.printInfo();

        String name = target.name();

        for(int i = 0; i < size; i++){
            log.append("[Segment] [");
            log.append(i + 1);
            log.append("/");
            log.append(size);
            log.append("] target[");
            log.append(name);
            log.append("]");
            log.printInfo();

            segments.get(i).prepareDebug(target, log);
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

        log.append("[");
        log.append(getClass().getSimpleName());
        log.append("]\n");
        log.printInfo();

        String name = target.name();

        for(int i = 0; i < size; i++){
            log.append("[Segment] [");
            log.append(i + 1);
            log.append("/");
            log.append(size);
            log.append("] target[");
            log.append(name);
            log.append("]");
            log.printInfo();

            segments.get(i).bufferDebug(target, log);
        }
    }

    public void upload(){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            segments.get(i).upload();
        }
    }

    public VLListType<FSBufferSegment<?>> get(){
        return segments;
    }

    public int size(){
        return segments.size();
    }
}
