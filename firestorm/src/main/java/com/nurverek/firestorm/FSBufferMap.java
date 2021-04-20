package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLDebug;
import vanguard.VLListType;

public final class FSBufferMap{

    protected VLListType<FSBufferLayout<?>> layouts;

    public FSBufferMap(){
        layouts = new VLListType<>(FSHub.ELEMENT_TOTAL_COUNT, FSHub.ELEMENT_TOTAL_COUNT);
    }

    public <BUFFER extends VLBuffer<?, ?>> FSBufferLayout<BUFFER> add(FSBufferLayout<BUFFER> layout){
        layouts.add(layout);
        return layout;
    }

    public void accountFor(FSMesh target){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).accountFor(target);
        }
    }

    public void buffer(FSMesh target){
        int size = layouts.size();

        for(int i = 0; i < size; i++){
            layouts.get(i).buffer(target);
        }
    }

    public void bufferDebug(FSMesh target){
        int size = layouts.size();

        VLDebug.append("[BufferLayout]\n");

        for(int i = 0; i < size; i++){
            VLDebug.append("Layout[");
            VLDebug.append(i);
            VLDebug.append("] ");
            VLDebug.printD();

            try{
                layouts.get(i).bufferDebug(target);

            }catch(Exception ex){
                VLDebug.append(" [FAILED]\n");
                throw ex;
            }

            VLDebug.append(" [SUCCESS]\n");
        }

        VLDebug.append("\n");
    }
}
