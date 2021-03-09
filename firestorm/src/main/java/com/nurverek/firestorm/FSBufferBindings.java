package com.nurverek.firestorm;

import vanguard.VLBufferTrackerType;
import vanguard.VLListType;

public class FSBufferBindings{

    protected VLListType<VLBufferTrackerType>[] trackers;

    protected FSBufferBindings(){
        trackers = new VLListType[FSG.ELEMENT_TOTAL_COUNT];

        for(int i = 0; i < trackers.length; i++){
            trackers[i] = new VLListType<>(1, 2);
        }
    }

    public void add(int element, VLBufferTrackerType e){
        trackers[element].add(e);
    }

    public void set(int element, int index, VLBufferTrackerType e){
        trackers[element].set(index, e);
    }

    public VLListType<VLBufferTrackerType> get(int element){
        return trackers[element];
    }

    public VLBufferTrackerType get(int element, int index){
        return trackers[element].get(index);
    }

    public int size(int element){
        return trackers[element].size();
    }
}
