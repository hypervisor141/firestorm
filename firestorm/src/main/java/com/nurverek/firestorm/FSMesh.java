package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLSyncer;

public class FSMesh extends VLSyncer.Syncable{

    protected String name;

    protected VLListType<FSInstance> instances;
    protected VLArrayShort indices;

    protected long id;
    protected int drawmode;

    
    public FSMesh(int drawmode, int initialcapacity, int resizer){
        this.drawmode = drawmode;

        instances = new VLListType<>(initialcapacity, resizer);
        id = FSControl.getNextID();
    }


    public void add(FSInstance instance){
        instances.add(instance);
        instance.mesh = this;
    }

    public void drawMode(int mode){
        drawmode = mode;
    }

    public void name(String name){
        this.name = name;
    }

    public void indices(VLArrayShort array){
        indices = array;
    }

    public FSInstance first(){
        return instances.get(0);
    }

    public FSBufferTracker firstAddress(){
        return instances.get(0).buffers;
    }

    public FSInstance get(int index){
        return instances.get(index);
    }

    public FSInstance remove(int index){
        FSInstance instance = instances.remove(index);
        instance.mesh = null;

        return instance;
    }


    public int drawMode(){
        return drawmode;
    }

    public String name(){
        return name;
    }

    public VLListType<FSInstance> instances(){
        return instances;
    }

    public VLArrayShort indices(){
        return indices;
    }

    public long id(){
        return id;
    }

    public int size(){
        return instances.size();
    }
}
