package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;
import vanguard.VLLog;

public class FSMesh implements VLCopyable<FSMesh>, FSInstanceOperator{

    public static final long FLAG_UNIQUE_ID = 0x10L;
    public static final long FLAG_UNIQUE_NAME = 0x100L;
    public static final long FLAG_FORCE_DUPLICATE_INSTANCES = 0x1000L;
    public static final long FLAG_DUPLICATE_CONFIGS = 0x100000L;

    protected VLListType<FSInstance> instances;
    protected VLListType<FSBufferBinding<?>>[] bindings;
    protected FSConfigGroup configs;
    protected String name;

    protected int drawmode;
    protected long id;

    public FSMesh(){

    }

    public FSMesh(FSMesh src, long flags){
        copy(src, flags);
    }

    public void initialize(int drawmode, int capacity, int resizer){
        this.drawmode = drawmode;

        instances = new VLListType<>(capacity, resizer);
        bindings = new VLListType[FSGlobal.COUNT];
        id = FSControl.getNextID();
    }

    public void initConfigs(FSConfig.Mode mode, int capacity, int resizer){
        this.configs = new FSConfigGroup(mode, capacity, resizer);
    }

    public FSInstance generateInstance(String name){
        FSInstance instance = new FSInstance(this, name);
        instances.add(instance);

        return instance;
    }

    public void add(FSConfig config){
        configs.add(config);
    }

    public void drawMode(int mode){
        drawmode = mode;
    }

    public void name(String name){
        this.name = name;
    }

    public FSInstance first(){
        return instances.get(0);
    }

    public FSInstance get(int index){
        return instances.get(index);
    }

    public FSConfig config(int index){
        return configs.configs().get(index);
    }

    public FSConfigGroup configs(){
        return configs;
    }

    public void allocateBinding(int element, int capacity, int resizer){
        if(bindings[element] == null){
            bindings[element] = new VLListType<>(capacity, resizer);
        }
    }

    public void bindFromStorage(int instanceindex, int element, int storageindex, int bindingindex){
        bindings[element].add(instances.get(instanceindex).storage().get(element).get(storageindex).bindings.get(bindingindex));
    }

    public void bindFromStorageLatest(int instanceindex, int element, int storageindex){
        VLListType<?> list = instances.get(instanceindex).storage().get(element).get(storageindex).bindings;
        bindings[element].add((FSBufferBinding<?>)list.get(list.size() - 1));
    }

    public void bindFromActive(int instanceindex, int element, int bindingindex){
        bindings[element].add(instances.get(instanceindex).element(element).bindings.get(bindingindex));
    }

    public void bindManual(int element, FSBufferBinding<?> binding){
        bindings[element].add(binding);
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

    public FSInstance remove(int index){
        FSInstance instance = instances.get(index);
        instances.remove(index);
        instance.mesh = null;

        return instance;
    }

    public int drawMode(){
        return drawmode;
    }

    public String name(){
        return name;
    }

    public VLListType<FSInstance> get(){
        return instances;
    }

    public long id(){
        return id;
    }

    public int size(){
        return instances.size();
    }

    public void configure(FSRPass pass, FSP program, int meshindex, int passindex){
        if(configs != null){
            configs.configure(pass, program, this, meshindex, passindex);
        }
    }

    public void configureDebug(FSRPass pass, FSP program, int meshindex, int passindex, VLLog log, int debug){
        if(configs != null){
            configs.configureDebug(pass, program, this, meshindex, passindex, log, debug);
        }
    }

    protected void scanComplete(FSInstance instance){
        instance.scanComplete();
    }

    public void bufferComplete(FSInstance instance, int index, int element, int storeindex){
        instance.bufferComplete(index, element, storeindex);
    }

    public void scanComplete(){}

    public void bufferComplete(){}

    public void programPreBuild(FSP program, FSP.CoreConfig core, int debug){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).programPreBuild(program, core, debug);
        }
    }

    @Override
    public void storeElement(int element, FSElement<?, ?> data){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).storeElement(element, data);
        }
    }

    @Override
    public void activateFirstElement(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).activateFirstElement(element);
        }
    }

    @Override
    public void activateLastElement(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).activateLastElement(element);
        }
    }

    @Override
    public void material(FSLightMaterial material){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).material(material);
        }
    }

    @Override
    public void lightMap(FSLightMap map){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).lightMap(map);
        }
    }

    @Override
    public void colorTexture(FSTexture tex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).colorTexture(tex);
        }
    }

    @Override
    public void updateSchematicBoundaries(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateSchematicBoundaries();
        }
    }

    @Override
    public void markSchematicsForUpdate(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).markSchematicsForUpdate();
        }
    }

    @Override
    public void applyModelMatrix(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).applyModelMatrix();
        }
    }

    @Override
    public void updateBuffer(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBuffer(element);
        }
    }

    @Override
    public void updateVertexBuffer(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateVertexBuffer(element);
        }
    }

    @Override
    public void updateVertexBufferStrict(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateVertexBufferStrict(element);
        }
    }

    @Override
    public void updateBufferPipeline(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipeline(element);
        }
    }

    @Override
    public void updateBufferPipelineStrict(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineStrict(element);
        }
    }

    @Override
    public void copy(FSMesh src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            instances = src.instances;
            configs = src.configs;
            name = src.name;
            drawmode = src.drawmode;
            id = src.id;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            instances = src.instances.duplicate(VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
            configs = src.configs.duplicate(VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
            name = src.name.concat("_duplicate").concat(String.valueOf(id));
            drawmode = src.drawmode;
            id = FSControl.getNextID();

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_FORCE_DUPLICATE_INSTANCES) == FLAG_FORCE_DUPLICATE_INSTANCES){
                instances = src.instances.duplicate(VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

            }else{
                instances = src.instances.duplicate(VLListType.FLAG_REFERENCE);
            }
            if((flags & FLAG_DUPLICATE_CONFIGS) == FLAG_DUPLICATE_CONFIGS){
                configs = src.configs.duplicate(FLAG_DUPLICATE);

            }else{
                configs = src.configs.duplicate(FLAG_REFERENCE);
            }
            if((flags & FLAG_UNIQUE_NAME) == FLAG_UNIQUE_NAME){
                name = src.name.concat("_duplicate").concat(String.valueOf(id));

            }else{
                name = src.name;
            }
            if((flags & FLAG_UNIQUE_ID) == FLAG_UNIQUE_ID){
                id = FSControl.getNextID();

            }else{
                id = src.id;
            }

            drawmode = src.drawmode;

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    @Override
    public FSMesh duplicate(long flags){
        return new FSMesh(this, flags);
    }

    public void destroy(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).destroy();
        }

        name = null;
        instances = null;
        bindings = null;
        configs = null;

        id = -1;
        drawmode = -1;
    }

}
