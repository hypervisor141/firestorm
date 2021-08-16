package hypervisor.firestorm.mesh;

import android.view.MotionEvent;

import hypervisor.firestorm.automation.FSBufferMap;
import hypervisor.firestorm.automation.FSHAssembler;
import hypervisor.firestorm.automation.FSScanFunction;
import hypervisor.firestorm.automation.FSScanTarget;
import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.engine.FSElements;
import hypervisor.firestorm.engine.FSGlobal;
import hypervisor.firestorm.engine.FSRPass;
import hypervisor.firestorm.program.FSLightMap;
import hypervisor.firestorm.program.FSLightMaterial;
import hypervisor.firestorm.program.FSP;
import hypervisor.firestorm.program.FSTexture;
import hypervisor.firestorm.tools.FSLog;
import hypervisor.vanguard.list.arraybacked.VLListType;
import hypervisor.vanguard.utils.VLCopyable;

public abstract class FSMesh<ENTRY extends FSTypeInstance> implements FSTypeMesh<ENTRY>{

    public static final long FLAG_UNIQUE_ID = 0x1L;
    public static final long FLAG_DUPLICATE_ENTRIES = 0x2L;
    public static final long FLAG_DUPLICATE_BINDINGS = 0x4L;
    public static final long FLAG_DUPLICATE_REFERENCE_BINDINGS = 0x8L;

    protected FSTypeRenderGroup<?> parent;
    protected VLListType<ENTRY> entries;
    protected VLListType<FSBufferBinding<?>>[] bindings;
    protected String name;
    protected long id;
    protected boolean enabled;
    protected boolean assembled;

    protected int globalbuffermapindex;
    protected int globalassemblerindex;
    protected int globalprogramindex;
    protected FSScanFunction scanfunction;

    public FSMesh(String name, int globalbuffermapindex, int globalassemblerindex, FSScanFunction scanfunction,
                  int globalprogramindex, int capacity, int resizeoverhead){

        this.name = name.toLowerCase();
        this.globalbuffermapindex = globalbuffermapindex;
        this.globalassemblerindex = globalassemblerindex;
        this.scanfunction = scanfunction;
        this.globalprogramindex = globalprogramindex;

        bindings = new VLListType[FSElements.COUNT];
        entries = new VLListType<>(capacity, resizeoverhead);
        id = FSControl.generateUID();

        enabled = true;
        assembled = false;
    }

    protected FSMesh(){

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
    public void bufferMap(int globalindex){
        this.globalbuffermapindex = globalindex;
    }

    @Override
    public void assembler(int globalindex){
        this.globalassemblerindex = globalindex;
    }

    @Override
    public void scanFunction(FSScanFunction function){
        this.scanfunction = function;
    }

    @Override
    public void program(int globalindex){
        if(globalprogramindex != -1){
            unregisterFromProgram();
        }

        this.globalprogramindex = globalindex;
    }

    @Override
    public void allocateBinding(int element, int capacity, int resizeoverhead){
        if(bindings[element] == null){
            bindings[element] = new VLListType<>(capacity, resizeoverhead);
        }
    }

    @Override
    public void bindManual(int element, FSBufferBinding<?> binding){
        bindings[element].add(binding);
    }

    @Override
    public FSBufferBinding<?> binding(int element, int index){
        return bindings[element].get(index);
    }

    @Override
    public void unbind(int element, int index){
        bindings[element].remove(index);
    }

    @Override
    public VLListType<FSBufferBinding<?>> bindings(int element){
        return bindings[element];
    }

    @Override
    public VLListType<FSBufferBinding<?>>[] bindings(){
        return bindings;
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
    public void remove(int index){
        ENTRY entry = entries.get(index);
        entries.remove(index);
        entry.parent(null);
    }

    @Override
    public void enable(boolean enabled){
        this.enabled = enabled;
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
    public FSHAssembler assembler(){
        return globalassemblerindex < 0 ? null : FSGlobal.GLOBAL.assembler(globalassemblerindex);
    }

    @Override
    public FSBufferMap bufferMap(){
        return globalbuffermapindex < 0 ? null : FSGlobal.GLOBAL.bufferMap(globalbuffermapindex);
    }

    @Override
    public FSScanFunction scanFunction(){
        return scanfunction;
    }

    @Override
    public FSP program(){
        return globalprogramindex < 0 ? null : FSGlobal.GLOBAL.program(globalprogramindex);
    }

    @Override
    public boolean enabled(){
        return enabled;
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
    public void autoScan(FSScanTarget target){
        try{
            target.scan((FSTypeMesh<FSTypeInstance>)this);

        }catch(Exception ex){
            throw new RuntimeException(ex);
        }

        scanComplete();
    }

    @Override
    public void autoAccountForBufferCapacity(){
        bufferMap().accountFor((FSTypeMesh<FSTypeInstance>)this);
    }

    @Override
    public void autoAccountForBufferCapacityDebug(FSLog log){
        bufferMap().accountForDebug((FSTypeMesh<FSTypeInstance>)this, log);
    }

    @Override
    public void autoBuildBuffer(){
        bufferMap().buffer((FSTypeMesh<FSTypeInstance>)this);
    }

    @Override
    public void autoBuildBufferDebug(FSLog log){
        bufferMap().bufferDebug((FSTypeMesh<FSTypeInstance>)this, log);
    }

    @Override
    public void autoUploadBuffer(){
        bufferMap().upload();
    }

    @Override
    public void autoBuild(){
        autoAccountForBufferCapacity();
        autoBuildBuffer();
        autoUploadBuffer();
        bufferComplete();

        registerWithPrograms();
        buildComplete();
    }

    @Override
    public void autoBuildDebug(FSLog log){
        autoAccountForBufferCapacityDebug(log);
        autoBuildBufferDebug(log);
        autoUploadBuffer();
        bufferComplete();

        registerWithPrograms();
        buildComplete();
    }

    @Override
    public void autoScanBuild(FSScanTarget target){
        autoScan(target);
        autoBuild();
    }

    @Override
    public void autoScanBuildDebug(FSScanTarget target, FSLog log){
        autoScan(target);
        autoBuildDebug(log);
    }

    @Override
    public void registerWithPrograms(){
        program().meshes().add(this);
    }

    @Override
    public void unregisterFromProgram(){
        program().meshes().remove(this);
    }

    @Override
    public void configure(FSP program, FSRPass pass, int targetindex, int passindex){}

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
        FSMesh<ENTRY> target = (FSMesh<ENTRY>)src;

        enabled = target.enabled;
        name = target.name;
        globalbuffermapindex = target.globalbuffermapindex;
        globalassemblerindex = target.globalassemblerindex;
        scanfunction = target.scanfunction;
        globalprogramindex = target.globalprogramindex;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            entries = target.entries;
            bindings = new VLListType[FSElements.COUNT];
            id = target.id;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            entries = target.entries.duplicate(VLCopyable.FLAG_DUPLICATE);
            bindings = new VLListType[FSElements.COUNT];
            id = FSControl.generateUID();

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_DUPLICATE_ENTRIES) == FLAG_DUPLICATE_ENTRIES){
                entries = target.entries.duplicate(VLCopyable.FLAG_DUPLICATE);

            }else{
                entries = target.entries.duplicate(VLListType.FLAG_REFERENCE);
            }

            if(target.bindings != null){
                if((flags & FLAG_DUPLICATE_BINDINGS) == FLAG_DUPLICATE_BINDINGS){
                    int size = target.bindings.length;
                    bindings = new VLListType[size];

                    for(int i = 0; i < size; i++){
                        VLListType<FSBufferBinding<?>> binding = target.bindings[i];

                        if(binding != null){
                            bindings[i] = binding.duplicate(VLCopyable.FLAG_DUPLICATE);
                        }
                    }

                }else if((flags & FLAG_DUPLICATE_REFERENCE_BINDINGS) == FLAG_DUPLICATE_REFERENCE_BINDINGS){
                    bindings = target.bindings.clone();

                }else{
                    bindings = new VLListType[FSElements.COUNT];
                }
            }

            if((flags & FLAG_UNIQUE_ID) == FLAG_UNIQUE_ID){
                id = FSControl.generateUID();

            }else{
                id = target.id;
            }

        }else{
            Helper.throwMissingAllFlags();
        }
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
        unregisterFromProgram();

        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).destroy();
        }
    }
}
