package com.nurverek.firestorm;

import vanguard.VLArrayShort;
import vanguard.VLListType;

public class FSMesh{

    protected String name;

    protected VLListType<FSInstance> instances;
    protected VLListType<FSLink<?>> links;
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
        id = FSCFrames.getNextID();
    }

    public void initLinks(VLListType<FSLink<?>> links){
        this.links = links;
    }

    public void addLink(FSLink<?> link){
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

    public FSLink<?> link(int index){
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

    public VLListType<FSLink<?>> links(){
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
