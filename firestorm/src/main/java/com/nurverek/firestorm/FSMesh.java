package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLSyncable;

public class FSMesh extends VLSyncable{

    protected String name;

    protected VLListType<FSInstance> instances;
    protected VLListType<FSLinkType> links;
    protected VLArrayShort indices;

    protected long id;
    protected int drawmode;

    public FSMesh(int drawmode, int capacity, int resizer){
        initialize(drawmode, capacity, resizer);
    }

    public FSMesh(){

    }

    public void initialize(int drawmode, int capacity, int resizer){
        this.drawmode = drawmode;

        instances = new VLListType<>(capacity, resizer);
        id = FSControl.getNextID();
    }

    public void initLinks(VLListType<FSLinkType> links){
        this.links = links;
    }

    public void addLink(FSLinkType link){
        links.add(link);
    }

    public void addInstance(FSInstance instance){
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

    public FSInstance instance(int index){
        return instances.get(index);
    }

    public FSLinkType link(int index){
        return links.get(index);
    }

    public FSInstance remove(int index){
        FSInstance instance = instances.get(index);
        instances.remove(index);
        instance.mesh = null;

        return instance;
    }

    public void removeLink(int index){
        links.remove(index);
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

    public VLListType<FSLinkType> links(){
        return links;
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

    public int sizeLinks(){
        return links.size();
    }
}
