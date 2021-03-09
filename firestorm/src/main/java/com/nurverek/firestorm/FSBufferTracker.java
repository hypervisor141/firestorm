package com.nurverek.firestorm;

import vanguard.VLListType;

public class FSBufferTracker{

    protected VLListType<FSBufferAddress>[] addresses;

    protected FSBufferTracker(){
        addresses = new VLListType[FSG.ELEMENT_TOTAL_COUNT];

        for(int i = 0; i < addresses.length; i++){
            addresses[i] = new VLListType<>(1, 2);
        }
    }

    public void add(int element, FSBufferAddress e){
        addresses[element].add(e);
    }

    public void set(int element, int index, FSBufferAddress e){
        addresses[element].set(index, e);
    }

    public VLListType<FSBufferAddress> get(int element){
        return addresses[element];
    }

    public FSBufferAddress get(int element, int index){
        return addresses[element].get(index);
    }

    public int size(int element){
        return addresses[element].size();
    }
}
