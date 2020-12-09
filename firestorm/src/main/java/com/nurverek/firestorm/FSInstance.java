package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLListType;

public class FSInstance{

    protected FSMesh mesh;
    protected FSSchematics schematics;
    protected FSMatrixModel modelmatrix;
    protected FSBufferTracker buffers;

    protected FSTexture colortexture;
    protected FSLightMaterial lightmaterial;
    protected FSLightMap lightmap;

    protected States states;
    protected Data data;

    protected long id;

    public FSInstance(){
        id = FSControl.getNextID();

        data = new Data();
        states = new States();
        schematics = new FSSchematics(this);
        buffers = new FSBufferTracker();
    }


    public void modelMatrix(FSMatrixModel set){
        modelmatrix = set;
    }

    public VLArrayFloat element(int element){
        return data.element(element);
    }

    public int elementVertexCount(int element){
        return element(element).size() / FSG.UNIT_SIZES[element];
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


    public long id(){
        return id;
    }

    public int vertexSize(){
        return data.positions().size() / FSG.UNIT_SIZE_POSITION;
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

    public FSBufferTracker bufferTracker(){
        return buffers;
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


    public static final class Data{

        public static final int DEFAULT_SIZE = FSG.ELEMENT_TOTAL_COUNT - 1;

        protected VLArrayFloat[] elements;

        public Data(){
            elements = new VLArrayFloat[DEFAULT_SIZE];
        }


        public void element(int element, VLArrayFloat array){
            elements[element] = array;
        }

        public void model(FSArrayModel array){
            elements[FSG.ELEMENT_MODEL] = array;
        }

        public void positions(VLArrayFloat array){
            elements[FSG.ELEMENT_POSITION] = array;
        }

        public void colors(VLArrayFloat array){
            elements[FSG.ELEMENT_COLOR] = array;
        }

        public void texCoords(VLArrayFloat array){
            elements[FSG.ELEMENT_TEXCOORD] = array;
        }

        public void normals(VLArrayFloat array){
            elements[FSG.ELEMENT_NORMAL] = array;
        }


        public VLArrayFloat element(int element){
            return elements[element];
        }

        public FSArrayModel model(){
            return (FSArrayModel)elements[FSG.ELEMENT_MODEL];
        }

        public VLArrayFloat positions(){
            return elements[FSG.ELEMENT_POSITION];
        }

        public VLArrayFloat colors(){
            return elements[FSG.ELEMENT_COLOR];
        }

        public VLArrayFloat texCoords(){
            return elements[FSG.ELEMENT_TEXCOORD];
        }

        public VLArrayFloat normals(){ return elements[FSG.ELEMENT_NORMAL]; }
    }

    public final class States{

        protected VLListType<Data> vault;
        protected int activeindex;

        protected States(){
            activeindex = 0;
        }


        public void initialize(int initialsize, int resizer){
            vault = new VLListType<>(initialsize, resizer);
        }

        public int currentActiveIndex(){
            return activeindex;
        }

        public void activateElement(int index, int element){
            data.element(element, vault.get(index).element(element));
        }

        public void activateData(int index){
            data = vault.get(index);
            activeindex = index;
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
