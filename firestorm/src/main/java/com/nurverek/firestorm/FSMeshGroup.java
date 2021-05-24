//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;

public class FSMeshGroup implements FSRenderableType<FSMeshGroup>{

    public static final long FLAG_UNIQUE_ID = 0x10L;
    public static final long FLAG_UNIQUE_NAME = 0x100L;
    public static final long FLAG_FORCE_DUPLICATE_LIST = 0x1000L;
    
    private VLListType<FSRenderableType<?>> group;
    private long id;
    private String name;

    public FSMeshGroup(String name, int capacity, int resizer){
        this.name = name;
        id = FSControl.getNextID();

        group = new VLListType<>(capacity, resizer);
    }

    public FSMeshGroup(FSMeshGroup src, long flags){
        this.copy(src, flags);
    }

    protected FSMeshGroup(){

    }

    public void add(FSRenderableType<?> mesh){
        group.add(mesh);
    }

    public VLListType<FSRenderableType<?>> get(){
        return group;
    }

    public FSRenderableType<?> get(int index){
        return group.get(index);
    }

    public int size(){
        return group.size();
    }

    @Override
    public String name(){
        return name;
    }

    @Override
    public long id(){
        return id;
    }

    @Override
    public void register(FSAutomator automator, FSGlobal global){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).register(automator, global);
        }
    }

    @Override
    public void scanComplete(){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).scanComplete();
        }
    }

    @Override
    public void bufferComplete(){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).bufferComplete();
        }
    }

    @Override
    public void buildComplete(){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).buildComplete();
        }
    }

    @Override
    public void allocateElement(int element, int capacity, int resizer){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).allocateElement(element, capacity, resizer);
        }
    }

    @Override
    public void storeElement(int element, FSElement<?, ?> data){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).storeElement(element, data);
        }
    }

    @Override
    public void activateFirstElement(int element){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).activateFirstElement(element);
        }
    }

    @Override
    public void activateLastElement(int element){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).activateLastElement(element);
        }
    }

    @Override
    public void material(FSLightMaterial material){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).material(material);
        }
    }

    @Override
    public void lightMap(FSLightMap map){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).lightMap(map);
        }
    }

    @Override
    public void colorTexture(FSTexture tex){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).colorTexture(tex);
        }
    }

    @Override
    public void updateSchematicBoundaries(){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).updateSchematicBoundaries();
        }
    }

    @Override
    public void markSchematicsForUpdate(){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).markSchematicsForUpdate();
        }
    }

    @Override
    public void applyModelMatrix(){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).applyModelMatrix();
        }
    }

    @Override
    public void updateBuffer(int element){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).updateBuffer(element);
        }
    }

    @Override
    public void copy(FSMeshGroup src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            id = FSControl.getNextID();
            group = src.group;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            id = FSControl.getNextID();
            group.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_FORCE_DUPLICATE_LIST) == FLAG_FORCE_DUPLICATE_LIST){
                group = src.group.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

            }else{
                group = src.group.duplicate(VLCopyable.FLAG_REFERENCE);
            }
            if((flags & FLAG_UNIQUE_ID) == FLAG_UNIQUE_ID){
                id = FSControl.getNextID();

            }else{
                id = src.id;
            }
            if((flags & FLAG_UNIQUE_NAME) == FLAG_UNIQUE_NAME){
                name = src.name.concat("_duplicate").concat(String.valueOf(id));

            }else{
                name = src.name;
            }

        }else{
            Helper.throwMissingAllFlags();
        }

        name = src.name;
    }

    @Override
    public FSMeshGroup duplicate(long flags){
        return new FSMeshGroup(this, flags);
    }

    @Override
    public void destroy(){
        int size = group.size();

        for(int i = 0; i < size; ++i){
            group.get(i).destroy();
        }
    }
}
