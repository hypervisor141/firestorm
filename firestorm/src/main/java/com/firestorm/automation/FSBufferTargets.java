package com.firestorm.automation;

import com.firestorm.mesh.FSInstance;
import com.firestorm.mesh.FSMesh;

import vanguard.list.VLListType;
import vanguard.utils.VLLog;

public class FSBufferTargets{

    protected VLListType<FSBufferSegment<?>> segments;

    public FSBufferTargets(int capacity){
        segments = new VLListType<>(capacity, capacity);
    }

    protected FSBufferTargets(){

    }

    public FSBufferTargets add(FSBufferSegment<?> segment){
        segments.add(segment);
        return this;
    }

    public void prepare(FSMesh<FSInstance> target){
        int size = segments.size();
        
        for(int i = 0; i < size; i++){
            segments.get(i).prepare(target);
        }
    }

    public void prepareDebug(FSMesh<FSInstance> target, VLLog log){
        int size = segments.size();
        log.addTag(getClass().getSimpleName());

        for(int i = 0; i < size; i++){
            log.addTag(String.valueOf(i));

            segments.get(i).prepareDebug(target, log);

            log.removeLastTag();
        }

        log.removeLastTag();
    }

    public void buffer(FSMesh<FSInstance> target){
        int size = segments.size();

        for(int i = 0; i < size; i++){
            segments.get(i).buffer(target);
        }
    }

    public void bufferDebug(FSMesh<FSInstance> target, VLLog log){
        int size = segments.size();
        log.addTag(getClass().getSimpleName());

        for(int i = 0; i < size; i++){
            log.addTag(String.valueOf(i));

            segments.get(i).bufferDebug(target, log);

            log.removeLastTag();
        }

        log.removeLastTag();
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
