package com.nurverek.firestorm;

import vanguard.VLListType;
import vanguard.VLLog;

public class FSMesh{

    protected String name;

    protected VLListType<FSInstance> instances;
    protected VLListType<FSBufferBinding<?>>[] activebindings;
    protected FSConfigGroup configs;

    protected long id;
    protected int drawmode;

    public FSMesh(){

    }

    public void initialize(int drawmode, int capacity, int resizer){
        this.drawmode = drawmode;

        activebindings = new VLListType[FSGlobal.COUNT];

        id = FSControl.getNextID();
        instances = new VLListType<>(capacity, resizer);
    }

    public void initConfigs(FSConfig.Mode mode, int capacity, int resizer){
        this.configs = new FSConfigGroup(mode, capacity, resizer);
    }

    public void scanComplete(FSInstance instance){}
    public void scanComplete(){}
    public void bufferComplete(int index, int element, FSInstance instance){}
    public void bufferComplete(){}
    public void programPreBuild(FSP program, FSP.CoreConfig core, int debug){}

    public FSInstance generateInstance(String name){
        FSInstance instance = new FSInstance(this, name);
        instances.add(instance);

        return instance;
    }

    public void add(FSConfig config){
        configs.add(config);
    }

    public void activateBinding(int element, FSBufferBinding<?> binding){
        activebindings[element].add(binding);
    }

    public void deactivateBinding(int element, int index){
        activebindings[element].remove(index);
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

    public FSConfig getConfig(int index){
        return configs.configs.get(index);
    }

    public FSConfigGroup configGroup(int index){
        return configs;
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

    public VLListType<FSBufferBinding<?>> bindings(int element){
        return activebindings[element];
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

    public void lightMaterial(FSLightMaterial material){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).lightMaterial(material);
        }
    }

    public void lightMap(FSLightMap map){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).lightMap(map);
        }
    }

    public void colorTexture(FSTexture tex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).colorTexture(tex);
        }
    }

    public void updateSchematicBoundaries(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateSchematicBoundaries();
        }
    }

    public void markSchematicsForUpdate(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).markSchematicsForUpdate();
        }
    }

    public void applyModelMatrices(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).applyModelMatrix();
        }
    }

    public void updateBuffer(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBuffer(element);
        }
    }

    public void updateVertexBuffer(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateVertexBuffer(element);
        }
    }

    public void updateVertexBufferStrict(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateVertexBufferStrict(element);
        }
    }

    public void updateBufferPipeline(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipeline(element);
        }
    }

    public void updateBufferPipelineStrict(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineStrict(element);
        }
    }

    public void destroy(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).destroy();
        }

        name = null;
        instances = null;
        configs = null;

        id = -1;
        drawmode = -1;
    }

}
