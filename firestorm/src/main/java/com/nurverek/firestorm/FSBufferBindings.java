package com.nurverek.firestorm;

import vanguard.VLBufferTrackerDetailed;
import vanguard.VLListType;

public class FSBufferBindings{

    protected VLListType<VLBufferTrackerDetailed<?>>[] trackers;

    protected FSBufferBindings(){
        trackers = new VLListType[FSG.ELEMENT_TOTAL_COUNT];

        for(int i = 0; i < trackers.length; i++){
            trackers[i] = new VLListType<>(1, 2);
        }
    }

    public void add(int element, VLBufferTrackerDetailed<?> entry){
        trackers[element].add(entry);
    }

    public void set(int element, int index, VLBufferTrackerDetailed<?> entry){
        trackers[element].set(index, entry);
    }

    public VLListType<VLBufferTrackerDetailed<?>> get(int element){
        return trackers[element];
    }

    public VLBufferTrackerDetailed<?> get(int element, int index){
        return trackers[element].get(index);
    }

    public int size(int element){
        return trackers[element].size();
    }
}
