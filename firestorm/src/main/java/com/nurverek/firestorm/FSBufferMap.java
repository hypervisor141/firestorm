package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLDebug;
import vanguard.VLListType;

public final class FSBufferMap{

    protected VLListType<FSBufferSegment<?>> segments;

    public FSBufferMap(){
        segments = new VLListType<>(FSHub.ELEMENT_TOTAL_COUNT, FSHub.ELEMENT_TOTAL_COUNT);
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

    public void bufferDebug(FSMesh target){
        int size = segments.size();

        VLDebug.append("[BufferLayout]\n");

        for(int i = 0; i < size; i++){
            VLDebug.append("Layout[");
            VLDebug.append(i);
            VLDebug.append("] ");
            VLDebug.printD();

            try{
                segments.get(i).bufferDebug(target);

            }catch(Exception ex){
                VLDebug.append(" [FAILED]\n");
                throw ex;
            }

            VLDebug.append(" [SUCCESS]\n");
        }

        VLDebug.append("\n");
    }
}
