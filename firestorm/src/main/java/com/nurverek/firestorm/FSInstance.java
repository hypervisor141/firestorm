package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLCopyable;

public class FSInstance implements VLCopyable<FSInstance>{

    public static final long FLAG_UNIQUE_ID = 0x10L;
    public static final long FLAG_UNIQUE_NAME = 0x100L;
    public static final long FLAG_SHALLOW_STORAGE = 0x1000L;
    public static final long FLAG_SHALLOW_STORAGE_ELEMENTS = 0x2000L;
    public static final long FLAG_SHALLOW_SCHEMATIC_BOUNDS = 0x10000L;
    public static final long FLAG_REFERENCE_MATERIAL = 0x100000L;
    public static final long FLAG_REFERENCE_LIGHTMAP = 0x1000000L;
    public static final long FLAG_REFERENCE_MODEL_MATRIX = 0x10000000L;
    public static final long FLAG_SHALLOW_MODEL_MATRIX = 0x20000000L;

    protected FSMesh mesh;
    protected FSElementStore store;
    protected FSSchematics schematics;
    protected FSMatrixModel modelmatrix;
    protected FSTexture colortexture;
    protected FSLightMaterial lightmaterial;
    protected FSLightMap lightmap;

    protected String name;
    protected long id;

    protected FSInstance(FSMesh mesh, String name){
        this.name = name;
        this.mesh = mesh;

        store = new FSElementStore();
        id = FSControl.getNextID();
        schematics = new FSSchematics(this);
    }

    protected FSInstance(FSInstance src, long flags){
        copy(src, flags);
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

    public void colorTexture(FSTexture texture){
        this.colortexture = texture;
    }

    public void lightMaterial(FSLightMaterial material){
        this.lightmaterial = material;
    }

    public void lightMap(FSLightMap map){
        this.lightmap = map;
    }

    public void activateElement(int element, int index){
        store.activate(element, index);
    }

    public String name(){
        return name;
    }

    public long id(){
        return id;
    }

    public int vertexSize(){
        return positions().size() / FSGlobal.UNIT_SIZES[FSGlobal.ELEMENT_POSITION];
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

    public FSMatrixModel modelMatrix(){
        return modelmatrix;
    }

    public int elementUnitsCount(int element){
        return element(element).size() / FSGlobal.UNIT_SIZES[element];
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
        return (FSArrayModel)elementData(FSGlobal.ELEMENT_MODEL);
    }

    public VLArrayFloat positions(){
        return (VLArrayFloat)elementData(FSGlobal.ELEMENT_POSITION);
    }

    public VLArrayFloat colors(){
        return (VLArrayFloat)elementData(FSGlobal.ELEMENT_COLOR);
    }

    public VLArrayFloat texCoords(){
        return (VLArrayFloat)elementData(FSGlobal.ELEMENT_TEXCOORD);
    }

    public VLArrayFloat normals(){
        return (VLArrayFloat)elementData(FSGlobal.ELEMENT_NORMAL);
    }

    public VLArrayShort indices(){
        return (VLArrayShort)elementData(FSGlobal.ELEMENT_INDEX);
    }

    public FSElement.Float modelEntry(){
        return (FSElement.Float)element(FSGlobal.ELEMENT_MODEL);
    }

    public FSElement.Float positionsEntry(){
        return (FSElement.Float)element(FSGlobal.ELEMENT_POSITION);
    }

    public FSElement.Float colorsEntry(){
        return (FSElement.Float)element(FSGlobal.ELEMENT_COLOR);
    }

    public FSElement.Float texCoordsEntry(){
        return (FSElement.Float)element(FSGlobal.ELEMENT_TEXCOORD);
    }

    public FSElement.Float normalsEntry(){
        return (FSElement.Float)element(FSGlobal.ELEMENT_NORMAL);
    }

    public FSElement.Short indicesEntry(){
        return (FSElement.Short)element(FSGlobal.ELEMENT_INDEX);
    }

    public void updateSchematicBoundaries(){
        schematics.updateBoundaries();
    }

    public void markSchematicsForUpdate(){
        schematics.markForNewUpdates();
    }

    public void applyModelMatrix(){
        model().transform(0, modelmatrix, true);
    }

    public void updateBuffer(int element){
        element(element).updateBuffer();
    }

    public void updateBuffer(int element, int bindingindex){
        element(element).updateBuffer(bindingindex);
    }

    public void updateVertexBuffer(int element){
        element(element).updateVertexBuffer();
    }

    public void updateVertexBuffer(int element, int bindingindex){
        element(element).updateVertexBuffer(bindingindex);
    }

    public void updateVertexBufferStrict(int element){
        element(element).updateVertexBufferStrict();
    }

    public void updateVertexBufferStrict(int element, int bindingindex){
        element(element).updateVertexBufferStrict(bindingindex);
    }

    public void updateBufferPipeline(int element){
        element(element).updateBufferPipeline();
    }

    public void updateBufferPipeline(int element, int bindingindex){
        element(element).updateBufferPipeline(bindingindex);
    }

    public void updateBufferPipelineStrict(int element){
        element(element).updateBufferPipelineStrict();
    }

    public void updateBufferPipelineStrict(int element, int bindingindex){
        element(element).updateBufferPipelineStrict(bindingindex);
    }

    @Override
    public void copy(FSInstance src, long flags){
        if((flags & FLAG_MINIMAL) == FLAG_MINIMAL){
            store = src.store;
            schematics = src.schematics;
            modelmatrix = src.modelmatrix;
            colortexture = src.colortexture;
            lightmaterial = src.lightmaterial;
            lightmap = src.lightmap;
            name = src.name;
            id = src.id;

        }else if((flags & FLAG_MAX_DEPTH) == FLAG_MAX_DEPTH){
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
            if((flags & FLAG_SHALLOW_STORAGE) == FLAG_SHALLOW_STORAGE){
                store = store.duplicate(FSElementStore.FLAG_SHALLOW_VAULT);

            }else if((flags & FLAG_SHALLOW_STORAGE_ELEMENTS) == FLAG_SHALLOW_STORAGE_ELEMENTS){
                store = store.duplicate(FSElementStore.FLAG_SHALLOW_VAULT);
            }

            public static final long FLAG_UNIQUE_NAME = 0x100L;
            public static final long FLAG_SHALLOW_STORAGE = 0x1000L;
            public static final long FLAG_SHALLOW_STORAGE_ELEMENTS = 0x2000L;
            public static final long FLAG_SHALLOW_SCHEMATIC_BOUNDS = 0x10000L;
            public static final long FLAG_REFERENCE_MATERIAL = 0x100000L;
            public static final long FLAG_REFERENCE_LIGHTMAP = 0x1000000L;
            public static final long FLAG_REFERENCE_MODEL_MATRIX = 0x10000000L;
            public static final long FLAG_SHALLOW_MODEL_MATRIX = 0x20000000L;

        }else{
            throw new RuntimeException("Invalid flags : " + flags);
        }

        mesh = src.mesh;
    }

    @Override
    public FSInstance duplicate(long flags){
        return new FSInstance(this, flags);
    }

    public void destroy(){
        mesh = null;
        schematics = null;
        modelmatrix = null;
        colortexture = null;
        lightmaterial = null;
        lightmap = null;
        store = null;
        name = null;

        id = -1;
    }
}
