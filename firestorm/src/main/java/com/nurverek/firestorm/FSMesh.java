package com.nurverek.firestorm;

import vanguard.VLArrayShort;
import vanguard.VLBufferTrackerDetailed;
import vanguard.VLListType;

public class FSMesh<INSTANCE extends FSInstance>{

    protected String name;

    protected VLListType<INSTANCE> instances;
    protected VLListType<FSLink<?>> links;
    protected FSConfigGroup configs;
    protected VLArrayShort indices;

    protected long id;
    protected int drawmode;

    public FSMesh(){

    }

    public void initialize(int drawmode, int capacity, int resizer){
        this.drawmode = drawmode;

        instances = new VLListType<>(capacity, resizer);
        id = FSCFrames.getNextID();
    }

    public void initLinks(int capacity, int resizer){
        this.links = new VLListType<>(capacity, resizer);
    }

    public void initConfigs(FSConfig.Mode mode, int capacity, int resizer){
        this.configs = new FSConfigGroup(mode, capacity, resizer);
    }

    public void scanComplete(){}

    public void bufferComplete(){}

    public INSTANCE generateInstance(String name){
        INSTANCE instance = (INSTANCE)new FSInstance(name);
        add(instance);

        return instance;
    }

    public void add(FSLink<?> link){
        links.add(link);
    }

    public void add(FSConfig config){
        configs.add(config);
    }

    public void add(INSTANCE instance){
        instances.add(instance);
        instance.mesh = (FSMesh<FSInstance>)this;
    }

    public void configure(FSRPass pass, FSP program, int meshindex, int passindex){
        if(configs != null){
            configs.configure(pass, program, this, meshindex, passindex);
        }
    }

    public void lightMaterial(FSLightMaterial material){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).lightMaterial(material);
        }
    }

    public void applyModelMatrices(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).applyModelMatrix();
        }
    }

    public void updateBufferDirectAll(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferDirectAll();
        }
    }

    public void updateVertexBufferAll(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateVertexBufferAll();
        }
    }

    public void updateBufferPipelineAll(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineAll();
        }
    }

    public void updateBufferPipelineStrictAll(){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineStrictAll();
        }
    }

    public void updateBufferPipeline(int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipeline(bufferindex);
        }
    }

    public void updateBufferPipelineStrict(int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineStrict(bufferindex);
        }
    }

    public void updateBufferDirect(int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferDirect(bufferindex);
        }
    }

    public void updateBufferVertex(int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferVertex(bufferindex);
        }
    }

    public void updateBufferVertexStrict(int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferVertexStrict(bufferindex);
        }
    }

    public void updateBufferDirectAll(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferDirectAll(element);
        }
    }

    public void updateVertexBufferAll(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateVertexBufferAll(element);
        }
    }

    public void updateBufferPipelineAll(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineAll(element);
        }
    }

    public void updateBufferPipelineStrictAll(int element){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineStrictAll(element);
        }
    }

    public void updateBufferPipeline(int element, int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipeline(element, bufferindex);
        }
    }

    public void updateBufferPipelineStrict(int element, int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferPipelineStrict(element, bufferindex);
        }
    }

    public void updateBufferDirect(int element, int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferDirect(element, bufferindex);
        }
    }

    public void updateBufferVertex(int element, int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferVertex(element, bufferindex);
        }
    }

    public void updateBufferVertexStrict(int element, int bufferindex){
        int size = instances.size();

        for(int i = 0; i < size; i++){
            instances.get(i).updateBufferVertexStrict(element, bufferindex);
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

    public INSTANCE first(){
        return instances.get(0);
    }

    public INSTANCE get(int index){
        return instances.get(index);
    }

    public FSLink<?> getLink(int index){
        return links.get(index);
    }

    public FSConfig getConfig(int index){
        return configs.configs.get(index);
    }

    public VLListType<FSLink<?>> links(int index){
        return links;
    }

    public FSConfigGroup configGroup(int index){
        return configs;
    }

    public INSTANCE remove(int index){
        INSTANCE instance = instances.get(index);
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

    public VLListType<INSTANCE> instances(){
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
