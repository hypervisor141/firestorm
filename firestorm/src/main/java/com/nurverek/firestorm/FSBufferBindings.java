package com.nurverek.firestorm;

import vanguard.VLBuffer;
import vanguard.VLBufferTracker;
import vanguard.VLListType;

public class FSBufferBindings{

    protected VLListType<Binding<?>>[] trackers;

    protected FSBufferBindings(){
        trackers = new VLListType[FSHub.ELEMENT_TOTAL_COUNT];

        for(int i = 0; i < trackers.length; i++){
            trackers[i] = new VLListType<>(1, 2);
        }
    }

    public void add(int element, Binding<?> entry){
        trackers[element].add(entry);
    }

    public void set(int element, int index, Binding<?> entry){
        trackers[element].set(index, entry);
    }

    public VLListType<Binding<?>> get(int element){
        return trackers[element];
    }

    public Binding<?> get(int element, int index){
        return trackers[element].get(index);
    }

    public int size(int element){
        return trackers[element].size();
    }

    public static final class Binding<BUFFER extends VLBuffer<?, ?>>{

        public VLBufferTracker tracker;
        public BUFFER buffer;
        public FSVertexBuffer<BUFFER> vbuffer;

        public Binding(VLBufferTracker tracker, BUFFER buffer, FSVertexBuffer<BUFFER> vbuffer){
            this.tracker = tracker;
            this.buffer = buffer;
            this.vbuffer = vbuffer;
        }
    }
}
