package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLSyncer;

public class FSMesh extends VLSyncer.Syncable{

    protected String name;

    protected VLListType<FSInstance> instances;
    protected VLListType<FSLink> links;
    protected VLArrayShort indices;

    protected long id;
    protected int drawmode;

    public FSMesh(int drawmode, int instancecapacity, int instanceresizer){
        this.drawmode = drawmode;

        instances = new VLListType<>(instancecapacity, instanceresizer);
        id = FSControl.getNextID();
    }

    public void add(FSInstance instance){
        instances.add(instance);
        instance.mesh = this;
    }

    public void add(FSLink link){
        links.add(link);
    }

    public void attachLinksToHosts(){
        int size = links.size();

        for(int i = 0; i < size; i++){
            links.get(i).attach();
        }
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

    public void links(VLListType<FSLink> links){
        this.links = links;
    }

    public FSLink link(int index){
        return links.get(index);
    }

    public FSInstance remove(int index){
        FSInstance instance = instances.remove(index);
        instance.mesh = null;

        return instance;
    }

    public FSLink removeLink(int index){
        return links.remove(index);
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

    public VLListType<FSLink> links(){
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
