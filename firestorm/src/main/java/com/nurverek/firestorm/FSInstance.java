package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLBufferTrackerDetailed;
import vanguard.VLListType;

public class FSInstance{

    protected FSMesh mesh;
    protected FSSchematics schematics;
    protected FSMatrixModel modelmatrix;
    protected FSBufferBindings bufferbindings;

    protected FSTexture colortexture;
    protected FSLightMaterial lightmaterial;
    protected FSLightMap lightmap;

    protected States states;
    protected Data data;

    protected String name;

    protected long id;
    protected int activatestate;

    public FSInstance(String name){
        this.name = name;

        id = FSCFrames.getNextID();
        activatestate = 0;

        data = new Data();
        states = new States();
        schematics = new FSSchematics(this);
        bufferbindings = new FSBufferBindings();
    }

    public FSInstance(FSInstance src){
        copy(src);
    }


    public void modelMatrix(FSMatrixModel set){
        modelmatrix = set;
    }

    public void name(String name){
        this.name = name;
    }

    public VLArrayFloat element(int element){
        return data.element(element);
    }

    public int elementVertexCount(int element){
        return element(element).size() / FSHub.UNIT_SIZES[element];
    }

    public void colorTexture(FSTexture texture){
        this.colortexture = texture;
    }

    public void lightMaterial(FSLightMaterial material){
        this.lightmaterial = material;
    }

    public void lightMap(FSLightMap map){
        this.lightmap = map;
    }

    public void activateState(int index){
        activatestate = index;
        data = states.get(index);
    }

    public void activateStateClone(int index){
        activatestate = index;
        data = new Data(states.get(index));
    }

    public void applyModelMatrix(){
        model().transform(0, modelmatrix, true);
    }

    public void updateBufferDirectAll(){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferDirectAll(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i]);
        }
    }

    public void updateVertexBufferAll(){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateVertexBufferAll(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i]);
        }
    }

    public void updateBufferPipelineAll(){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferPipelineAll(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i]);
        }
    }

    public void updateBufferPipelineStrictAll(){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferPipelineStrictAll(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i]);
        }
    }

    public void updateBufferPipeline(int bufferindex){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferPipeline(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i], bufferindex);
        }
    }

    public void updateBufferPipelineStrict(int bufferindex){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferPipelineStrict(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i], bufferindex);
        }
    }

    public void updateBufferDirect(int bufferindex){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferDirect(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i], bufferindex);
        }
    }

    public void updateBufferVertex(int bufferindex){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferVertex(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i], bufferindex);
        }
    }

    public void updateBufferVertexStrict(int bufferindex){
        int size = FSHub.ELEMENTS_LIST_INSTANCE_BASED.length;

        for(int i = 0; i < size; i++){
            updateBufferVertexStrict(FSHub.ELEMENTS_LIST_INSTANCE_BASED[i], bufferindex);
        }
    }

    public void updateBufferDirectAll(int element){
        int size = bufferbindings.get(element).size();

        for(int i = 0; i < size; i++){
            updateBufferDirect(element, i);
        }
    }

    public void updateVertexBufferAll(int element){
        int size = bufferbindings.get(element).size();

        for(int i = 0; i < size; i++){
            updateBufferVertex(element, i);
        }
    }

    public void updateBufferPipelineAll(int element){
        int size = bufferbindings.get(element).size();

        for(int i = 0; i < size; i++){
            updateBufferPipeline(element, i);
        }
    }

    public void updateBufferPipelineStrictAll(int element){
        int size = bufferbindings.get(element).size();

        for(int i = 0; i < size; i++){
            updateBufferPipelineStrict(element, i);
        }
    }

    public void updateBufferPipeline(int element, int bufferindex){
        updateBufferDirect(element, bufferindex);
        updateBufferVertex(element, bufferindex);
    }

    public void updateBufferPipelineStrict(int element, int bufferindex){
        updateBufferDirect(element, bufferindex);
        updateBufferVertexStrict(element, bufferindex);
    }

    public void updateBufferDirect(int element, int bufferindex){
        FSBufferBindings.Binding<?> binding = bufferbindings.get(element).get(bufferindex);
        binding.buffer.update(binding.tracker, element(element).provider());
    }

    public void updateBufferVertex(int element, int bufferindex){
        bufferbindings.get(element).get(bufferindex).vbuffer.update();
    }

    public void updateBufferVertexStrict(int element, int bufferindex){
        FSBufferBindings.Binding<?> binding = bufferbindings.get(element).get(bufferindex);
        VLBufferTrackerDetailed tracker = binding.tracker;
        int offset = tracker.offset();

        binding.vbuffer.update(offset, tracker.stride() * tracker.count());
    }

    public String name(){
        return name;
    }

    public long id(){
        return id;
    }

    public int vertexSize(){
        return data.positions().size() / FSHub.UNIT_SIZE_POSITION;
    }

    public FSMesh mesh(){
        return mesh;
    }

    public FSTexture colorTexture(){
        return colortexture;
    }

    public FSLightMaterial lightMaterial(){
        return lightmaterial;
    }

    public FSLightMap lightMap(){
        return lightmap;
    }

    public FSSchematics schematics(){
        return schematics;
    }

    public FSBufferBindings bufferBindings(){
        return bufferbindings;
    }

    public FSMatrixModel modelMatrix(){
        return modelmatrix;
    }

    public States states(){
        return states;
    }

    public Data data(){
        return data;
    }

    public FSArrayModel model(){
        return data.model();
    }

    public VLArrayFloat positions(){
        return data.positions();
    }

    public VLArrayFloat colors(){
        return data.colors();
    }

    public VLArrayFloat texCoords(){
        return data.texCoords();
    }

    public VLArrayFloat normals(){
        return data.normals();
    }

    public VLArrayShort indices(){
        return mesh.indices;
    }

    public void copy(FSInstance src){
        id = FSCFrames.getNextID();

        name = src.name;
        data = new Data(src.data);
        states = new States(src.states);
        schematics = new FSSchematics(this, src.schematics);
        bufferbindings = new FSBufferBindings();
    }


    public static final class Data{

        public static final int DEFAULT_SIZE = FSHub.ELEMENT_TOTAL_COUNT - 1;

        protected VLArrayFloat[] elements;

        public Data(){
            elements = new VLArrayFloat[DEFAULT_SIZE];
        }

        public Data(Data src){
            copy(src);
        }

        public void element(int element, VLArrayFloat array){
            elements[element] = array;
        }

        public void model(FSArrayModel array){
            elements[FSHub.ELEMENT_MODEL] = array;
        }

        public void positions(VLArrayFloat array){
            elements[FSHub.ELEMENT_POSITION] = array;
        }

        public void colors(VLArrayFloat array){
            elements[FSHub.ELEMENT_COLOR] = array;
        }

        public void texCoords(VLArrayFloat array){
            elements[FSHub.ELEMENT_TEXCOORD] = array;
        }

        public void normals(VLArrayFloat array){
            elements[FSHub.ELEMENT_NORMAL] = array;
        }


        public VLArrayFloat element(int element){
            return elements[element];
        }

        public FSArrayModel model(){
            return (FSArrayModel)elements[FSHub.ELEMENT_MODEL];
        }

        public VLArrayFloat positions(){
            return elements[FSHub.ELEMENT_POSITION];
        }

        public VLArrayFloat colors(){
            return elements[FSHub.ELEMENT_COLOR];
        }

        public VLArrayFloat texCoords(){
            return elements[FSHub.ELEMENT_TEXCOORD];
        }

        public VLArrayFloat normals(){ return elements[FSHub.ELEMENT_NORMAL]; }

        public void copy(Data src){
            elements = new VLArrayFloat[DEFAULT_SIZE];

            for(int i = 0; i < elements.length; i++){
                VLArrayFloat array = src.elements[i];

                if(array != null){
                    elements[i] = new VLArrayFloat(array.provider().clone());
                }
            }
        }

    }

    public static final class States{

        protected VLListType<Data> vault;

        protected States(){

        }

        protected States(States src){
            initialize(src);
        }

        public void initialize(int initialsize, int resizer){
            vault = new VLListType<>(initialsize, resizer);
        }

        public void initialize(States src){
            int size = src.vault.size();
            vault = new VLListType<>(size, src.vault.resizerCount());

            for(int i = 0; i < size; i++){
                vault.add(new Data(src.vault.get(i)));
            }
        }

        public void add(Data state){
            vault.add(state);
        }

        public Data get(int index){
            return vault.get(index);
        }

        public void set(int index, Data data){
            vault.set(index, data);
        }

        public void remove(int index){
            vault.remove(index);
        }

        public VLListType<Data> get(){
            return vault;
        }
    }
}
