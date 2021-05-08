package com.nurverek.firestorm;

import vanguard.VLListType;
import vanguard.VLLog;

public class FSBufferTarget{

    protected VLListType<FSBufferMap<?>> maps;
    private boolean initialized;
    private boolean uploaded;

    public FSBufferTarget(int capacity, int resizer){
        maps = new VLListType<>(capacity, resizer);

        initialized = false;
        uploaded = false;
    }

    public void accountFor(FSMesh target){
        int size = maps.size();

        for(int i = 0; i < size; i++){
            maps.get(i).accountFor(target);
        }
    }

    public void initialize(){
        if(!initialized){
            int size = maps.size();

            for(int i = 0; i < size; i++){
                maps.get(i).initialize();
            }

            initialized = true;
        }
    }

    public void upload(){
        if(!uploaded){
            int size = maps.size();

            for(int i = 0; i < size; i++){
                maps.get(i).upload();
            }

            uploaded = true;
        }
    }

    public void buffer(FSMesh target){
        int size = maps.size();

        for(int i = 0; i < size; i++){
            maps.get(i).buffer(target);
        }
    }

    public void bufferDebug(FSMesh mesh, VLLog log){
        int size = maps.size();

        for(int i = 0; i < size; i++){
            maps.get(i).bufferDebug(mesh, log);
        }
    }

    public FSBufferTarget add(FSBufferMap<?> map){
        maps.add(map);
        return this;
    }

    public VLListType<FSBufferMap<?>> get(){
        return maps;
    }

    public int size(){
        return maps.size();
    }
}
