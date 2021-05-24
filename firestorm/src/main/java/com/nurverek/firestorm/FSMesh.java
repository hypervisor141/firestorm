package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;
import vanguard.VLLog;

public class FSMesh<TYPE extends FSRenderableType> implements FSRenderableType{

    public static final long FLAG_UNIQUE_ID = 0x10L;
    public static final long FLAG_UNIQUE_NAME = 0x100L;
    public static final long FLAG_FORCE_DUPLICATE_entryS = 0x1000L;
    public static final long FLAG_DUPLICATE_CONFIGS = 0x100000L;

    protected FSRenderableType parent;
    protected VLListType<TYPE> entries;
    protected VLListType<FSBufferBinding<?>>[] bindings;
    protected FSConfig configs;
    protected String name;

    protected long id;

    public FSMesh(String name, int capacity, int resizer){
        this.name = name;

        bindings = new VLListType[FSElementRegisry.COUNT];
        entries = new VLListType<>(capacity, resizer);
        id = FSControl.getNextID();
    }

    public FSMesh(FSMesh<TYPE> src, long flags){
        copy(src, flags);
    }

    protected FSMesh(){

    }

    public void register(FSAutomator automator, String name, FSGlobal global){

    }

    public void add(TYPE entry){
        entries.add(entry);
        entry.parent(this);
    }

    public void bindManual(int element, FSBufferBinding<?> binding){
        bindings[element].add(binding);
    }

    public void name(String name){
        this.name = name;
    }

    @Override
    public void parent(FSRenderableType parent){
        this.parent = parent;
    }

    public void allocateBinding(int element, int capacity, int resizer){
        if(bindings[element] == null){
            bindings[element] = new VLListType<>(capacity, resizer);
        }
    }

    public void unbind(int element, int index){
        bindings[element].remove(index);
    }

    public FSBufferBinding<?> binding(int element, int index){
        return bindings[element].get(index);
    }

    public VLListType<FSBufferBinding<?>> bindings(int element){
        return bindings[element];
    }

    public VLListType<FSBufferBinding<?>>[] bindings(){
        return bindings;
    }

    public TYPE first(){
        return entries.get(0);
    }

    public TYPE get(int index){
        return entries.get(index);
    }

    @Override
    public FSConfig configs(){
        return configs;
    }

    public void remove(TYPE entry){
        remove(entries.indexOf(entry));
    }

    public TYPE remove(int index){
        TYPE entry = entries.get(index);
        entries.remove(index);
        entry.parent(null);

        return entry;
    }

    public VLListType<TYPE> get(){
        return entries;
    }

    @Override
    public FSRenderableType parent(){
        return parent;
    }

    @Override
    public FSRenderableType parentRoot(){
        return parent == null ? null : parent.parentRoot();
    }

    @Override
    public String name(){
        return name;
    }

    @Override
    public long id(){
        return id;
    }

    public int size(){
        return entries.size();
    }

    @Override
    public void run(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex){
        if(configs != null){
            configs.run(pass, program, mesh, meshindex, passindex);
        }

        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).run(pass, program, mesh, meshindex, passindex);
        }
    }

    @Override
    public void runDebug(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex, VLLog log, int debug){
        if(configs != null){
            configs.runDebug(pass, program, mesh, meshindex, passindex, log, debug);
        }

        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).runDebug(pass, program, mesh, meshindex, passindex, log, debug);
        }
    }

    @Override
    public void scanComplete(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).scanComplete();
        }
    }

    @Override
    public void buildComplete(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).buildComplete();
        }
    }

    @Override
    public void allocateElement(int element, int capacity, int resizer){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).allocateElement(element, capacity, resizer);
        }
    }

    @Override
    public void storeElement(int element, FSElement<?, ?> data){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).storeElement(element, data);
        }
    }

    @Override
    public void activateFirstElement(int element){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).activateFirstElement(element);
        }
    }

    @Override
    public void activateLastElement(int element){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).activateLastElement(element);
        }
    }

    @Override
    public void material(FSLightMaterial material){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).material(material);
        }
    }

    @Override
    public void lightMap(FSLightMap map){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).lightMap(map);
        }
    }

    @Override
    public void colorTexture(FSTexture tex){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).colorTexture(tex);
        }
    }

    @Override
    public void updateSchematicBoundaries(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).updateSchematicBoundaries();
        }
    }

    @Override
    public void markSchematicsForUpdate(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).markSchematicsForUpdate();
        }
    }

    @Override
    public void applyModelMatrix(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).applyModelMatrix();
        }
    }

    @Override
    public void updateBuffer(int element){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).updateBuffer(element);
        }
    }

    @Override
    public void copy(FSRenderableType src, long flags){
        FSMesh<TYPE> target = (FSMesh<TYPE>)src;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            entries = target.entries;
            configs = target.configs;
            name = target.name;
            id = target.id;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            entries = target.entries.duplicate(VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
            configs = target.configs.duplicate(VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
            name = target.name.concat("_duplicate").concat(String.valueOf(id));
            id = FSControl.getNextID();

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_FORCE_DUPLICATE_entryS) == FLAG_FORCE_DUPLICATE_entryS){
                entries = target.entries.duplicate(VLCopyable.FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

            }else{
                entries = target.entries.duplicate(VLListType.FLAG_REFERENCE);
            }

            if((flags & FLAG_UNIQUE_ID) == FLAG_UNIQUE_ID){
                id = FSControl.getNextID();

            }else{
                id = target.id;
            }

            if((flags & FLAG_UNIQUE_NAME) == FLAG_UNIQUE_NAME){
                name = target.name.concat("_duplicate").concat(String.valueOf(id));

            }else{
                name = target.name;
            }

            if(target.configs != null && (flags & FLAG_DUPLICATE_CONFIGS) == FLAG_DUPLICATE_CONFIGS){
                configs = target.configs.duplicate(FLAG_DUPLICATE);
            }

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    @Override
    public FSRenderableType duplicate(long flags){
        return new FSMesh<TYPE>(this, flags);
    }

    @Override
    public void destroy(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).destroy();
        }

        parent = null;
        entries = null;
        bindings = null;
        configs = null;
        name = null;

        id = -1;
    }

}
