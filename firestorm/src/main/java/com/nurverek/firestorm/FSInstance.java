package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayInt;
import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLFloat;
import com.nurverek.vanguard.VLInt;
import com.nurverek.vanguard.VLListDouble;
import com.nurverek.vanguard.VLListFloat;
import com.nurverek.vanguard.VLListInt;
import com.nurverek.vanguard.VLListLong;
import com.nurverek.vanguard.VLListShort;
import com.nurverek.vanguard.VLListType;
import com.nurverek.vanguard.VLShort;

public class FSInstance{

    protected FSMesh mesh;
    protected FSSchematics schematics;
    protected FSModelCluster modelcluster;
    protected FSMeshBuffers address;

    protected FSTexture colortexture;
    protected FSLightMaterial lightmaterial;
    protected FSLightMap lightmap;

    protected States states;
    protected Data data;

    protected long id;

    public FSInstance(int customssize, int customsresizer){
        id = FSControl.getNextID();

        data = new Data(customssize, customsresizer);
        states = new States();
        schematics = new FSSchematics(this);
        address = new FSMeshBuffers();
    }


    public void modelCluster(FSModelCluster set){
        modelcluster = set;
    }

    public VLArrayFloat element(int element){
        return data.element(element);
    }

    public int elementVertexCount(int element){
        return element(element).size() / FSLoader.UNIT_SIZES[element];
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
        return data.positions().size() / FSLoader.UNIT_SIZE_POSITION;
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

    public FSMeshBuffers bufferAddress(){
        return address;
    }

    public FSModelCluster modelCluster(){
        return modelcluster;
    }

    public States states(){
        return states;
    }

    public Data data(){
        return data;
    }

    public FSModelArray model(){
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

        public static final int DEFAULT_SIZE = FSLoader.ELEMENT_TOTAL_COUNT - 1;

        protected VLArrayFloat[] elements;
        protected VLListType<CustomData> customs;

        public Data(int customssize, int customsresizer){
            elements = new VLArrayFloat[DEFAULT_SIZE];
            customs = new VLListType<>(customssize, customsresizer);
        }


        public void element(int element, VLArrayFloat array){
            elements[element] = array;
        }

        public void model(FSModelArray array){
            elements[FSLoader.ELEMENT_MODEL] = array;
        }

        public void positions(VLArrayFloat array){
            elements[FSLoader.ELEMENT_POSITION] = array;
        }

        public void colors(VLArrayFloat array){
            elements[FSLoader.ELEMENT_COLOR] = array;
        }

        public void texCoords(VLArrayFloat array){
            elements[FSLoader.ELEMENT_TEXCOORD] = array;
        }

        public void normals(VLArrayFloat array){
            elements[FSLoader.ELEMENT_NORMAL] = array;
        }


        public VLArrayFloat element(int element){
            return elements[element];
        }

        public FSModelArray model(){
            return (FSModelArray)elements[FSLoader.ELEMENT_MODEL];
        }

        public VLArrayFloat positions(){
            return elements[FSLoader.ELEMENT_POSITION];
        }

        public VLArrayFloat colors(){
            return elements[FSLoader.ELEMENT_COLOR];
        }

        public VLArrayFloat texCoords(){
            return elements[FSLoader.ELEMENT_TEXCOORD];
        }

        public VLArrayFloat normals(){ return elements[FSLoader.ELEMENT_NORMAL]; }



        public void set(int index, CustomData data){
            customs.set(index, data);
        }

        public void add(CustomData data){
            customs.add(data);
        }

        public CustomData get(int index){
            return customs.get(index);
        }

        public VLListType<CustomData> get(){
            return customs;
        }

        public int sizeCustoms(){
            return customs.size();
        }
    }

    public static final class CustomData{

        private Object data;

        public CustomData(Object data){
            this.data = data;
        }


        public void set(Object data){
            this.data = data;
        }


        public Object get(){
            return data;
        }

        public VLShort asShort(){
            return (VLShort)data;
        }

        public VLInt asInt(){
            return (VLInt)data;
        }

        public VLFloat asFloat(){
            return (VLFloat)data;
        }

        public VLArrayShort asArrayShort(){
            return (VLArrayShort)data;
        }

        public VLArrayInt asArrayInt(){
            return (VLArrayInt)data;
        }

        public VLArrayFloat asArrayFloat(){
            return (VLArrayFloat)data;
        }

        public VLListType asList(){
            return (VLListType)data;
        }

        public VLListShort asListShort(){
            return (VLListShort)data;
        }

        public VLListInt asListInt(){
            return (VLListInt)data;
        }

        public VLListLong asListLong(){
            return (VLListLong)data;
        }

        public VLListFloat asListFloat(){
            return (VLListFloat)data;
        }

        public VLListDouble asListDouble(){
            return (VLListDouble)data;
        }
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

        public Data remove(int index){
            return vault.remove(index);
        }

        public VLListType<Data> get(){
            return vault;
        }
    }
}
