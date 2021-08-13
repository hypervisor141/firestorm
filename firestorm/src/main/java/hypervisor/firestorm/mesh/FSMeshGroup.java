package hypervisor.firestorm.mesh;

import android.view.MotionEvent;

import hypervisor.firestorm.automation.FSHScanner;
import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.engine.FSGlobal;
import hypervisor.firestorm.program.FSLightMap;
import hypervisor.firestorm.program.FSLightMaterial;
import hypervisor.firestorm.program.FSTexture;
import hypervisor.vanguard.list.arraybacked.VLListType;
import hypervisor.vanguard.utils.VLCopyable;

public class FSMeshGroup<ENTRY extends FSTypeRenderGroup<?>> implements FSTypeMeshGroup<ENTRY>{
    
    public static final long FLAG_UNIQUE_ID = 0x1L;
    public static final long FLAG_UNIQUE_NAME = 0x2L;
    public static final long FLAG_DUPLICATE_ENTRIES = 0x4L;

    protected FSTypeRenderGroup<?> parent;
    protected VLListType<ENTRY> entries;
    protected String name;
    protected long id;
    protected boolean assembled;

    public FSMeshGroup(String name, int capacity, int resizeoverhead){
        this.name = name.toLowerCase();

        entries = new VLListType<>(capacity, resizeoverhead);
        id = FSControl.generateUID();
        assembled = false;
    }

    public FSMeshGroup(FSMeshGroup<ENTRY> src, long flags){
        copy(src, flags);
    }

    protected FSMeshGroup(){

    }

    @Override
    public void construct(FSGlobal global){

    }

    @Override
    public void assemble(FSGlobal global){
        if(!assembled){
            construct(global);

            int size = entries.size();

            for(int i = 0; i < size; i++){
                entries.get(i).assemble(global);
            }

            assembled = true;
        }
    }

    @Override
    public void name(String name){
        this.name = name;
    }

    @Override
    public void parent(FSTypeRenderGroup<?> parent){
        this.parent = parent;
    }

    @Override
    public void add(ENTRY entry){
        entries.add(entry);
        entry.parent(this);
    }

    @Override
    public ENTRY first(){
        return entries.get(0);
    }

    @Override
    public ENTRY last(){
        return entries.get(entries.size() - 1);
    }

    @Override
    public void remove(ENTRY entry){
        remove(entries.indexOf(entry));
    }

    @Override
    public void enable(boolean enabled){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).enable(enabled);
        }
    }

    @Override
    public void remove(int index){
        ENTRY entry = entries.get(index);
        entries.remove(index);
        entry.parent(null);
    }

    @Override
    public ENTRY get(int index){
        return entries.get(index);
    }

    @Override
    public VLListType<ENTRY> get(){
        return entries;
    }

    @Override
    public boolean enabled(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            if(entries.get(i).enabled()){
                return true;
            }
        }

        return false;
    }

    @Override
    public FSTypeRenderGroup<?> parent(){
        return parent;
    }

    @Override
    public FSTypeRenderGroup<?> parentRoot(){
        return parent == null ? this : parent.parentRoot();
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
    public boolean assembled(){
        return assembled;
    }

    @Override
    public int size(){
        return entries.size();
    }

    @Override
    public void scanComplete(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).scanComplete();
        }
    }

    @Override
    public void bufferComplete(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).bufferComplete();
        }
    }

    @Override
    public void buildComplete(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).buildComplete();
        }
    }

    @Override
    public void register(FSHScanner<?> scanner){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).register(scanner);
        }
    }

    @Override
    public void allowReassmbly(){
        assembled = false;

        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).allowReassmbly();
        }
    }

    @Override
    public void allocateElement(int element, int capacity, int resizeoverhead){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).allocateElement(element, capacity, resizeoverhead);
        }
    }

    @Override
    public void storeElement(int element, FSElement<?, ?> data){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).storeElement(element, data);
        }
    }

    @Override
    public void activateFirstElement(int element){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).activateFirstElement(element);
        }
    }

    @Override
    public void activateLastElement(int element){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).activateLastElement(element);
        }
    }

    @Override
    public void material(FSLightMaterial material){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).material(material);
        }
    }

    @Override
    public void lightMap(FSLightMap map){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).lightMap(map);
        }
    }

    @Override
    public void colorTexture(FSTexture texture){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).colorTexture(texture);
        }
    }

    @Override
    public void updateBuffer(int element){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).updateBuffer(element);
        }
    }

    @Override
    public void applyModelMatrix(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).applyModelMatrix();
        }
    }

    @Override
    public void schematicsRebuild(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRebuild();
        }
    }

    @Override
    public void schematicsCheckFixLocalSpaceFlatness(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsCheckFixLocalSpaceFlatness();
        }
    }

    @Override
    public void schematicsRefillLocalSpaceBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRefillLocalSpaceBounds();
        }
    }

    @Override
    public void schematicsRebuildLocalSpaceCentroid(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRebuildLocalSpaceCentroid();
        }
    }



    @Override
    public void schematicsCheckSortLocalSpaceBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsCheckSortLocalSpaceBounds();
        }
    }

    @Override
    public void schematicsCheckSortModelSpaceBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsCheckSortModelSpaceBounds();
        }
    }

    @Override
    public void schematicsRequestFullUpdate(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRequestFullUpdate();
        }
    }

    @Override
    public void schematicsRequestUpdateModelSpaceBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRequestUpdateModelSpaceBounds();
        }
    }

    @Override
    public void schematicsRequestUpdateCentroid(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRequestUpdateCentroid();
        }
    }

    @Override
    public void schematicsRequestUpdateCollisionBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRequestUpdateCollisionBounds();
        }
    }

    @Override
    public void schematicsRequestUpdateInputBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsRequestUpdateInputBounds();
        }
    }

    @Override
    public void schematicsDirectReloadLocalSpaceBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsDirectReloadLocalSpaceBounds();
        }
    }

    @Override
    public void schematicsDirectUpdateModelSpaceBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsDirectUpdateModelSpaceBounds();
        }
    }

    @Override
    public void schematicsDirectUpdateCentroid(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsDirectUpdateCentroid();
        }
    }

    @Override
    public void schematicsDirectUpdateCollisionBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsDirectUpdateCollisionBounds();
        }
    }

    @Override
    public void schematicsDirectUpdateInputBounds(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).schematicsDirectUpdateInputBounds();
        }
    }

    @Override
    public void dispatch(Dispatch<FSTypeRender> dispatch){
        dispatch.process(this);
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).dispatch(dispatch);
        }
    }

    @Override
    public boolean checkInputs(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            if(entries.get(i).checkInputs(e1, e2, f1, f2, near, far)){
                return true;
            }
        }

        return false;
    }

    @Override
    public void copy(FSTypeRender src, long flags){
        FSMeshGroup<ENTRY> target = (FSMeshGroup<ENTRY>)src;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            entries = target.entries;
            id = target.id;
            name = target.name;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            entries = target.entries.duplicate(VLCopyable.FLAG_DUPLICATE);
            id = FSControl.generateUID();
            name = target.name.concat("_duplicate").concat(String.valueOf(id));

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_DUPLICATE_ENTRIES) == FLAG_DUPLICATE_ENTRIES){
                entries = target.entries.duplicate(VLCopyable.FLAG_DUPLICATE);

            }else{
                entries = target.entries.duplicate(VLListType.FLAG_REFERENCE);
            }

            if((flags & FLAG_UNIQUE_ID) == FLAG_UNIQUE_ID){
                id = FSControl.generateUID();

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
    public FSTypeRender duplicate(long flags){
        return new FSMeshGroup<>(this, flags);
    }

    @Override
    public void paused(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).paused();
        }
    }

    @Override
    public void resumed(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).resumed();
        }
    }

    @Override
    public void destroy(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).destroy();
        }
    }
}
