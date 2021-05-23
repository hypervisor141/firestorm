package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLCopyable;
import vanguard.VLVMatrix;

public class FSInstance implements VLCopyable<FSInstance>, FSMeshType{

    public static final long FLAG_UNIQUE_ID = 0x10L;
    public static final long FLAG_UNIQUE_NAME = 0x100L;
    public static final long FLAG_FORCE_DUPLICATE_STORAGE = 0x1000L;
    public static final long FLAG_DUPLICATE_SCHEMATICS = 0x10000L;
    public static final long FLAG_DUPLICATE_MATERIAL = 0x100000L;
    public static final long FLAG_DUPLICATE_MODEL_MATRIX = 0x10000000L;

    protected FSMesh mesh;
    protected FSElementStore store;
    protected FSSchematics schematics;
    protected FSMatrixModel modelmatrix;
    protected FSTexture colortexture;
    protected FSLightMaterial material;
    protected FSLightMap lightmap;

    protected String name;
    protected long id;

    public FSInstance(FSMesh mesh, String name){
        this.name = name;
        this.mesh = mesh;

        store = new FSElementStore();
        schematics = new FSSchematics(this);
        id = FSControl.getNextID();
    }

    public FSInstance(FSInstance src, long flags){
        copy(src, flags);
    }

    protected FSInstance(){

    }

    public void name(String name){
        this.name = name;
    }

    public void storage(FSElementStore store){
        this.store = store;
    }

    public void modelMatrix(FSMatrixModel set){
        modelmatrix = set;
    }

    @Override
    public void colorTexture(FSTexture texture){
        this.colortexture = texture;
    }

    @Override
    public void material(FSLightMaterial material){
        this.material = material;
    }

    @Override
    public void lightMap(FSLightMap map){
        this.lightmap = map;
    }

    @Override
    public void allocateElement(int element, int capacity, int resizer){
        store.allocateElement(element, capacity, resizer);
    }

    @Override
    public void storeElement(int element, FSElement<?, ?> data){
        store.add(element, data);
    }

    public void activateElement(int element, int index){
        store.activate(element, index);
    }

    @Override
    public void activateFirstElement(int element){
        store.activate(element, 0);
    }

    @Override
    public void activateLastElement(int element){
        store.activate(element, store.size(element) - 1);
    }

    public String name(){
        return name;
    }

    public long id(){
        return id;
    }

    public int vertexSize(){
        return positions().size() / FSElementRegisry.UNIT_SIZES[FSElementRegisry.ELEMENT_POSITION];
    }

    public FSMesh mesh(){
        return mesh;
    }

    public FSTexture colorTexture(){
        return colortexture;
    }

    public FSLightMaterial material(){
        return material;
    }

    public FSLightMap lightMap(){
        return lightmap;
    }

    public FSSchematics schematics(){
        return schematics;
    }

    public FSMatrixModel modelMatrix(){
        return modelmatrix;
    }

    public int elementUnitsCount(int element){
        return element(element).size() / FSElementRegisry.UNIT_SIZES[element];
    }

    public Object elementData(int element){
        return store.active(element).data;
    }

    public FSElement<?, ?> element(int element){
        return store.active(element);
    }

    public FSElementStore storage(){
        return store;
    }

    public FSArrayModel model(){
        return (FSArrayModel)elementData(FSElementRegisry.ELEMENT_MODEL);
    }

    public VLArrayFloat positions(){
        return (VLArrayFloat)elementData(FSElementRegisry.ELEMENT_POSITION);
    }

    public VLArrayFloat colors(){
        return (VLArrayFloat)elementData(FSElementRegisry.ELEMENT_COLOR);
    }

    public VLArrayFloat texCoords(){
        return (VLArrayFloat)elementData(FSElementRegisry.ELEMENT_TEXCOORD);
    }

    public VLArrayFloat normals(){
        return (VLArrayFloat)elementData(FSElementRegisry.ELEMENT_NORMAL);
    }

    public VLArrayShort indices(){
        return (VLArrayShort)elementData(FSElementRegisry.ELEMENT_INDEX);
    }

    public FSElement.FloatArray modelEntry(){
        return (FSElement.FloatArray)element(FSElementRegisry.ELEMENT_MODEL);
    }

    public FSElement.FloatArray positionsEntry(){
        return (FSElement.FloatArray)element(FSElementRegisry.ELEMENT_POSITION);
    }

    public FSElement.FloatArray colorsEntry(){
        return (FSElement.FloatArray)element(FSElementRegisry.ELEMENT_COLOR);
    }

    public FSElement.FloatArray texCoordsEntry(){
        return (FSElement.FloatArray)element(FSElementRegisry.ELEMENT_TEXCOORD);
    }

    public FSElement.FloatArray normalsEntry(){
        return (FSElement.FloatArray)element(FSElementRegisry.ELEMENT_NORMAL);
    }

    public FSElement.Short indicesEntry(){
        return (FSElement.Short)element(FSElementRegisry.ELEMENT_INDEX);
    }

    public void scanComplete(){}

    public void bufferComplete(int element, int storeindex){}

    public void programPreBuild(FSP program, FSP.CoreConfig core, int debug){}

    @Override
    public void updateSchematicBoundaries(){
        schematics.updateBoundaries();
    }

    @Override
    public void markSchematicsForUpdate(){
        schematics.markForNewUpdates();
    }

    @Override
    public void applyModelMatrix(){
        model().transform(0, modelmatrix, true);
    }

    @Override
    public void updateBuffer(int element){
        element(element).updateBuffer();
    }

    public void updateBuffer(int element, int bindingindex){
        element(element).updateBuffer(bindingindex);
    }

    public void updateVertexBuffer(int element, int bindingindex){
        element(element).bindings.get(bindingindex).updateVertexBuffer();
    }

    public void updateVertexBufferStrict(int element, int bindingindex){
        element(element).bindings.get(bindingindex).updateVertexBufferStrict();
    }

    @Override
    public void copy(FSInstance src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            store = src.store;
            schematics = src.schematics;
            modelmatrix = src.modelmatrix;
            colortexture = src.colortexture;
            material = src.material;
            lightmap = src.lightmap;
            name = src.name;
            id = src.id;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            store = src.store.duplicate(FLAG_DUPLICATE);
            schematics = src.schematics.duplicate(FLAG_DUPLICATE);

            if(modelmatrix != null){
                modelmatrix = src.modelmatrix.duplicate(VLVMatrix.FLAG_FORCE_DUPLICATE_ENTRIES);
            }
            if(material != null){
                material = src.material.duplicate(FLAG_DUPLICATE);
            }

            colortexture = src.colortexture;
            lightmap = src.lightmap;
            name = src.name.concat("_duplicate").concat(String.valueOf(id));
            id = FSControl.getNextID();

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            colortexture = src.colortexture;
            lightmap = src.lightmap;

            if((flags & FLAG_FORCE_DUPLICATE_STORAGE) == FLAG_FORCE_DUPLICATE_STORAGE){
                store = src.store.duplicate(FLAG_DUPLICATE);

            }else{
                store = src.store.duplicate(FLAG_REFERENCE);
            }
            if((flags & FLAG_DUPLICATE_SCHEMATICS) == FLAG_DUPLICATE_SCHEMATICS){
                schematics = src.schematics.duplicate(FLAG_DUPLICATE);

            }else{
                schematics = src.schematics.duplicate(FLAG_REFERENCE);
            }
            if(material != null && (flags & FLAG_DUPLICATE_MATERIAL) == FLAG_DUPLICATE_MATERIAL){
                material = src.material.duplicate(VLVMatrix.FLAG_FORCE_DUPLICATE_ENTRIES);

            }else{
                material = src.material.duplicate(FLAG_REFERENCE);
            }
            if(modelmatrix != null && (flags & FLAG_DUPLICATE_MODEL_MATRIX) == FLAG_DUPLICATE_MODEL_MATRIX){
                modelmatrix = src.modelmatrix.duplicate(VLVMatrix.FLAG_FORCE_DUPLICATE_ENTRIES);

            }else{
                modelmatrix = src.modelmatrix.duplicate(FLAG_REFERENCE);
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

        }else{
            Helper.throwMissingAllFlags();
        }

        mesh = src.mesh;
    }

    @Override
    public FSInstance duplicate(long flags){
        return new FSInstance(this, flags);
    }

    @Override
    public void destroy(){
        mesh = null;
        schematics = null;
        modelmatrix = null;
        colortexture = null;
        material = null;
        lightmap = null;
        store = null;
        name = null;

        id = -1;
    }
}
