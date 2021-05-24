package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLCopyable;
import vanguard.VLLog;
import vanguard.VLVMatrix;

public class FSInstance implements FSRenderableType{

    public static final long FLAG_UNIQUE_ID = 0x10L;
    public static final long FLAG_UNIQUE_NAME = 0x100L;
    public static final long FLAG_FORCE_DUPLICATE_STORAGE = 0x1000L;
    public static final long FLAG_DUPLICATE_SCHEMATICS = 0x10000L;
    public static final long FLAG_DUPLICATE_MATERIAL = 0x100000L;
    public static final long FLAG_DUPLICATE_MODEL_MATRIX = 0x10000000L;
    public static final long FLAG_DUPLICATE_CONFIGS = 0x100000000L;

    protected FSMesh<FSInstance> parent;
    protected FSElementStore store;
    protected FSSchematics schematics;
    protected FSMatrixModel modelmatrix;
    protected FSTexture colortexture;
    protected FSLightMaterial material;
    protected FSLightMap lightmap;
    protected FSConfig configs;

    protected String name;
    protected long id;

    public FSInstance(String name){
        this.name = name;

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
    public void parent(FSRenderableType parent){
        this.parent = (FSMesh<FSInstance>)parent;
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

    @Override
    public FSMesh<FSInstance> parent(){
        return parent;
    }

    @Override
    public FSRenderableType parentRoot(){
        return parent == null ? null : parent.parentRoot();
    }

    @Override
    public FSConfig configs(){
        return configs;
    }

    @Override
    public String name(){
        return name;
    }

    @Override
    public long id(){
        return id;
    }

    public int vertexSize(){
        return positions().size() / FSElementRegisry.UNIT_SIZES[FSElementRegisry.ELEMENT_POSITION];
    }

    public FSMesh<FSInstance> mesh(){
        return parent;
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

    @Override
    public void run(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex){
        if(configs != null){
            configs.run(pass, program, mesh, meshindex, passindex);
        }
    }

    @Override
    public void runDebug(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex, VLLog log, int debug){
        if(configs != null){
            configs.runDebug(pass, program, mesh, meshindex, passindex, log, debug);
        }
    }

    @Override
    public void scanComplete(){}

    @Override
    public void buildComplete(){}

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
    public void copy(FSRenderableType src, long flags){
        FSInstance target = (FSInstance)src;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            store = target.store;
            schematics = target.schematics;
            modelmatrix = target.modelmatrix;
            colortexture = target.colortexture;
            material = target.material;
            lightmap = target.lightmap;
            name = target.name;
            id = target.id;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            store = target.store.duplicate(FLAG_DUPLICATE);
            schematics = target.schematics.duplicate(FLAG_DUPLICATE);

            if(modelmatrix != null){
                modelmatrix = target.modelmatrix.duplicate(VLVMatrix.FLAG_FORCE_DUPLICATE_ENTRIES);
            }
            if(material != null){
                material = target.material.duplicate(FLAG_DUPLICATE);
            }

            colortexture = target.colortexture;
            lightmap = target.lightmap;
            name = target.name.concat("_duplicate").concat(String.valueOf(id));
            id = FSControl.getNextID();

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            colortexture = target.colortexture;
            lightmap = target.lightmap;

            if((flags & FLAG_FORCE_DUPLICATE_STORAGE) == FLAG_FORCE_DUPLICATE_STORAGE){
                store = target.store.duplicate(FLAG_DUPLICATE);

            }else{
                store = target.store.duplicate(FLAG_REFERENCE);
            }

            if((flags & FLAG_DUPLICATE_SCHEMATICS) == FLAG_DUPLICATE_SCHEMATICS){
                schematics = target.schematics.duplicate(FLAG_DUPLICATE);

            }else{
                schematics = target.schematics.duplicate(FLAG_REFERENCE);
            }

            if(target.material != null && (flags & FLAG_DUPLICATE_MATERIAL) == FLAG_DUPLICATE_MATERIAL){
                material = target.material.duplicate(VLCopyable.FLAG_DUPLICATE);
            }

            if(target.modelmatrix != null && (flags & FLAG_DUPLICATE_MODEL_MATRIX) == FLAG_DUPLICATE_MODEL_MATRIX){
                modelmatrix = target.modelmatrix.duplicate(VLVMatrix.FLAG_FORCE_DUPLICATE_ENTRIES);
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
                configs = configs.duplicate(VLCopyable.FLAG_DUPLICATE);
            }

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    @Override
    public FSInstance duplicate(long flags){
        return new FSInstance(this, flags);
    }

    @Override
    public void destroy(){
        parent = null;
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
