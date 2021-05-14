package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;

public class FSMeshGroup implements VLCopyable<FSMeshGroup>, FSMeshType{

    private VLListType<FSMesh> group;

    public FSMeshGroup(int capacity, int resizer){
        group = new VLListType<>(capacity, resizer);
    }

    public FSMeshGroup(FSMeshGroup src, long flags){
        copy(src, flags);
    }

    public VLListType<FSMesh> get(){
        return group;
    }

    public FSMesh get(int index){
        return group.get(index);
    }

    public void add(FSMesh mesh){
        group.add(mesh);
    }

    public int size(){
        return group.size();
    }

    @Override
    public void storeElement(int element, FSElement<?, ?> data){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).storeElement(element, data);
        }
    }

    @Override
    public void activateFirstElement(int element){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).activateFirstElement(element);
        }
    }

    @Override
    public void activateLastElement(int element){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).activateLastElement(element);
        }
    }

    @Override
    public void material(FSLightMaterial material){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).material(material);
        }
    }

    @Override
    public void lightMap(FSLightMap map){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).lightMap(map);
        }
    }

    @Override
    public void colorTexture(FSTexture tex){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).colorTexture(tex);
        }
    }

    @Override
    public void updateSchematicBoundaries(){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).updateSchematicBoundaries();
        }
    }

    @Override
    public void markSchematicsForUpdate(){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).markSchematicsForUpdate();
        }
    }

    @Override
    public void applyModelMatrix(){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).applyModelMatrix();
        }
    }

    @Override
    public void updateBuffer(int element){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).updateBuffer(element);
        }
    }

    @Override
    public void updateVertexBuffer(int element){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).updateVertexBuffer(element);
        }
    }

    @Override
    public void updateVertexBufferStrict(int element){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).updateVertexBufferStrict(element);
        }
    }

    @Override
    public void updateBufferPipeline(int element){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).updateBufferPipeline(element);
        }
    }

    @Override
    public void updateBufferPipelineStrict(int element){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).updateBufferPipelineStrict(element);
        }
    }

    @Override
    public void copy(FSMeshGroup src, long flags){

    }

    @Override
    public FSMeshGroup duplicate(long flags){
        return new FSMeshGroup(this, flags);
    }

    @Override
    public void destroy(){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).destroy();
        }
    }
}
