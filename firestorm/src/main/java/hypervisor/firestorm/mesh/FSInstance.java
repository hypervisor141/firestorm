package hypervisor.firestorm.mesh;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.engine.FSElements;
import hypervisor.firestorm.program.FSLightMap;
import hypervisor.firestorm.program.FSLightMaterial;
import hypervisor.firestorm.program.FSTexture;
import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.array.VLArrayShort;
import hypervisor.vanguard.utils.VLCopyable;
import hypervisor.vanguard.variable.VLVMatrix;

public class FSInstance implements FSTypeInstance{

    public static final long FLAG_UNIQUE_ID = 0x10L;
    public static final long FLAG_UNIQUE_NAME = 0x100L;
    public static final long FLAG_FORCE_DUPLICATE_STORAGE = 0x1000L;
    public static final long FLAG_DUPLICATE_SCHEMATICS = 0x10000L;
    public static final long FLAG_DUPLICATE_MATERIAL = 0x100000L;
    public static final long FLAG_DUPLICATE_MODEL_MATRIX = 0x10000000L;

    protected FSTypeRenderGroup<?> parent;
    protected FSElementStore store;
    protected FSSchematics schematics;
    protected FSMatrixModel modelmatrix;
    protected FSTexture colortexture;
    protected FSLightMaterial material;
    protected FSLightMap lightmap;

    protected String name;
    protected long id;

    public FSInstance(String name){
        this.name = name.toLowerCase();

        store = new FSElementStore(FSElements.COUNT);
        schematics = new FSSchematics(this);
        id = FSControl.getNextID();
    }

    public FSInstance(FSInstance src, long flags){
        copy(src, flags);
    }

    protected FSInstance(){

    }

    @Override
    public void name(String name){
        this.name = name;
    }

    @Override
    public void storage(FSElementStore store){
        this.store = store;
    }

    @Override
    public void modelMatrix(FSMatrixModel set){
        modelmatrix = set;
    }

    @Override
    public void parent(FSTypeRenderGroup<?> parent){
        this.parent = parent;
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
    public FSTypeRenderGroup<?> parent(){
        return parent;
    }

    @Override
    public FSTypeRenderGroup<?> parentRoot(){
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

    @Override
    public int vertexSize(){
        return positions().size() / FSElements.UNIT_SIZES[FSElements.ELEMENT_POSITION];
    }

    @Override
    public FSTexture colorTexture(){
        return colortexture;
    }

    @Override
    public FSLightMaterial material(){
        return material;
    }

    @Override
    public FSLightMap lightMap(){
        return lightmap;
    }

    @Override
    public FSMatrixModel modelMatrix(){
        return modelmatrix;
    }

    @Override
    public FSSchematics schematics(){
        return schematics;
    }

    @Override
    public FSElementStore storage(){
        return store;
    }

    @Override
    public int elementUnitsCount(int element){
        return element(element).size() / FSElements.UNIT_SIZES[element];
    }

    @Override
    public Object elementData(int element){
        return store.active(element).data;
    }

    @Override
    public FSElement<?, ?> element(int element){
        return store.active(element);
    }

    @Override
    public FSArrayModel model(){
        return (FSArrayModel)elementData(FSElements.ELEMENT_MODEL);
    }

    @Override
    public VLArrayFloat positions(){
        return (VLArrayFloat)elementData(FSElements.ELEMENT_POSITION);
    }

    @Override
    public VLArrayFloat colors(){
        return (VLArrayFloat)elementData(FSElements.ELEMENT_COLOR);
    }

    @Override
    public VLArrayFloat texCoords(){
        return (VLArrayFloat)elementData(FSElements.ELEMENT_TEXCOORD);
    }

    @Override
    public VLArrayFloat normals(){
        return (VLArrayFloat)elementData(FSElements.ELEMENT_NORMAL);
    }

    @Override
    public VLArrayShort indices(){
        return (VLArrayShort)elementData(FSElements.ELEMENT_INDEX);
    }

    @Override
    public FSElement.FloatArray modelElement(){
        return (FSElement.FloatArray)element(FSElements.ELEMENT_MODEL);
    }

    @Override
    public FSElement.FloatArray positionsElement(){
        return (FSElement.FloatArray)element(FSElements.ELEMENT_POSITION);
    }

    @Override
    public FSElement.FloatArray colorsElement(){
        return (FSElement.FloatArray)element(FSElements.ELEMENT_COLOR);
    }

    @Override
    public FSElement.FloatArray texCoordsElement(){
        return (FSElement.FloatArray)element(FSElements.ELEMENT_TEXCOORD);
    }

    @Override
    public FSElement.FloatArray normalsElement(){
        return (FSElement.FloatArray)element(FSElements.ELEMENT_NORMAL);
    }

    @Override
    public FSElement.Short indicesElement(){
        return (FSElement.Short)element(FSElements.ELEMENT_INDEX);
    }

    @Override
    public void updateBuffer(int element){
        element(element).updateBuffer();
    }

    @Override
    public void updateBuffer(int element, int bindingindex){
        element(element).updateBuffer(bindingindex);
    }

    @Override
    public void updateVertexBuffer(int element, int bindingindex){
        element(element).bindings.get(bindingindex).updateVertexBuffer();
    }

    @Override
    public void updateVertexBufferStrict(int element, int bindingindex){
        element(element).bindings.get(bindingindex).updateVertexBufferStrict();
    }

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
    public void dispatch(FSTypeRenderGroup.Dispatch<FSTypeRender> dispatch){
        dispatch.process(this);
    }

    @Override
    public void scanComplete(){}

    @Override
    public void bufferComplete(){}

    @Override
    public void buildComplete(){}

    @Override
    public void copy(FSTypeRender src, long flags){
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
