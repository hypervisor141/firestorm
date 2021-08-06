package hypervisor.firestorm.program;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.engine.FSRPass;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.utils.VLCopyable;
import hypervisor.vanguard.utils.VLLog;

public class FSConfigSequence extends FSConfigLocated{

    public static final long FLAG_DUPLICATE_CONFIGS = 0x1L;

    protected VLListType<FSConfig> configs;
    protected int glslsize;

    public FSConfigSequence(Mode mode, int capacity, int resizeoverhead){
        super(mode);
        configs = new VLListType<>(capacity, resizeoverhead);
    }

    public FSConfigSequence(Mode mode){
        super(mode);
    }

    protected FSConfigSequence(){

    }

    public FSConfigSequence(FSConfigSequence src, long flags){
        super(null);
        copy(src, flags);
    }

    public void add(FSConfig config){
        configs.add(config);
        glslsize += config.getGLSLSize();
    }

    public VLListType<FSConfig> configs(){
        return configs;
    }

    public void updateGLSLSize(){
        int size = configs.size();
        glslsize = 0;

        for(int i = 0; i < size; i++){
            glslsize += configs.get(i).getGLSLSize();
        }
    }

    @Override
    protected void notifyProgramBuilt(FSP program){
        int size = configs.size();

        for(int i = 0; i < size; i++){
            configs.get(i).notifyProgramBuilt(program);
        }
    }

    @Override
    public final void configure(FSP program, FSRPass pass, FSTypeMesh<?> mesh, int meshindex, int passindex){
        int size = configs.size();

        int loc = location;

        for(int i = 0; i < size; i++){
            FSConfig config = configs.get(i);
            config.location(loc);
            config.run(pass, program, mesh, meshindex, passindex);

            loc += config.getGLSLSize();
        }
    }

    @Override
    public final void configureDebug(FSP program, FSRPass pass, FSTypeMesh<?> mesh, int meshindex, int passindex, VLLog log, int debug){
        int size = configs.size();
        int loc = location;

        for(int i = 0; i < size; i++){
            log.addTag((i + 1) + "/" + size);

            FSConfig config = configs.get(i);
            config.location(loc);

            try{
                config.runDebug(pass, program, mesh, meshindex, passindex, log, debug);

            }catch(Exception ex){
                log.removeLastTag();
                throw new RuntimeException(ex);
            }

            loc += config.getGLSLSize();
            log.removeLastTag();
        }
    }

    @Override
    public int getGLSLSize(){
        return glslsize;
    }

    @Override
    public void copy(FSConfig src, long flags){
        super.copy(src, flags);

        FSConfigSequence target = (FSConfigSequence)src;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            configs = target.configs;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            configs = target.configs.duplicate(FLAG_DUPLICATE);

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            if((flags & FLAG_DUPLICATE_CONFIGS) == FLAG_DUPLICATE_CONFIGS){
                configs = target.configs.duplicate(FLAG_CUSTOM | VLListType.FLAG_DUPLICATE_ARRAY_FULLY);

            }else{
                VLCopyable.Helper.throwMissingSubFlags("FLAG_CUSTOM", "FLAG_DUPLICATE_CONFIGS");
            }

        }else{
            VLCopyable.Helper.throwMissingAllFlags();
        }

        glslsize = target.glslsize;
    }

    @Override
    public FSConfigSequence duplicate(long flags){
        return new FSConfigSequence(this, flags);
    }

    @Override
    public void attachDebugInfo(FSRPass pass, FSP program, FSTypeMesh<?> mesh, VLLog log, int debug){
        log.append("sequenceSize[");
        log.append(configs.size());
        log.append("] ");
    }
}
