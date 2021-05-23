package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;

public abstract class FSMeshGroup implements VLCopyable<FSMeshGroup>, FSMeshType{

    private VLListType<FSMeshType> group;

    public FSMeshGroup(int capacity, int resizer){
        group = new VLListType<>(capacity, resizer);
    }

    public FSMeshGroup(){

    }

    public VLListType<FSMeshType> get(){
        return group;
    }

    public FSMeshType get(int index){
        return group.get(index);
    }

    public void add(FSMeshType mesh){
        group.add(mesh);
    }

    public int size(){
        return group.size();
    }

    protected abstract void register(FSAutomator automator, FSRGlobal global);

    @Override
    public void allocateElement(int element, int capacity, int resizer){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).allocateElement(element, capacity, resizer);
        }
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
    public void copy(FSMeshGroup src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            group = src.group;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            group.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

        }else{
            VLCopyable.Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public void destroy(){
        int size = group.size();

        for(int i = 0; i < size; i++){
            group.get(i).destroy();
        }
    }
}
