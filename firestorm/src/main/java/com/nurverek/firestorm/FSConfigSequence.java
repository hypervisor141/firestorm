package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;
import vanguard.VLLog;

public class FSConfigSequence extends FSConfigLocated{

    public static final long FLAG_FORCE_DUPLICATE_CONFIGS = 0x1L;

    protected VLListType<FSConfig> configs;
    protected int glslsize;

    public FSConfigSequence(Mode mode, int capacity, int resizer){
        super(mode);
        configs = new VLListType<>(capacity, resizer);
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
    public final void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
        int size = configs.size();
        FSConfig c;

        int loc = location;

        for(int i = 0; i < size; i++){
            c = configs.get(i);
            c.location(loc);
            c.run(pass, program, mesh, meshindex, passindex);

            loc += c.getGLSLSize();
        }
    }

    @Override
    public final void configureDebug(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
        String classname = getClass().getSimpleName();
        int size = configs.size();
        int loc = location;

        log.append("ENTERING [");
        log.append(classname);
        log.append("] configs[");
        log.append(size);
        log.append("] location[");
        log.append(loc);
        log.append("] GLSLSize[");
        log.append(glslsize);
        log.append("]\n");

        for(int i = 0; i < size; i++){
            log.append("[");
            log.append(i + 1);
            log.append("/");
            log.append(size);
            log.append("] ");

            FSConfig config = configs.get(i);
            config.location(loc);
            config.runDebug(pass, program, mesh, meshindex, passindex, log, debug);

            loc += config.getGLSLSize();
        }

        log.append("EXITING [");
        log.append(classname);
        log.append("]\n");
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
            if((flags & FLAG_FORCE_DUPLICATE_CONFIGS) == FLAG_FORCE_DUPLICATE_CONFIGS){
                configs = target.configs.duplicate(FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);

            }else{
                Helper.throwMissingSubFlags("FLAG_CUSTOM", "FLAG_FORCE_DUPLICATE_CONFIGS");
            }

        }else{
            Helper.throwMissingAllFlags();
        }

        glslsize = target.glslsize;
    }

    @Override
    public FSConfigSequence duplicate(long flags){
        return new FSConfigSequence(this, flags);
    }

    @Override
    public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
        StringBuilder data = new StringBuilder();
        FSConfig c;

        int size = configs.size();

        data.append("sequence[");
        data.append(size);
        data.append("]");

        for(int i = 0; i < size; i++){
            c = configs.get(i);

            data.append("config[");
            data.append(i);
            data.append("] [");
            data.append(c.getClass().getSimpleName());

            if(debug >= FSControl.DEBUG_FULL){
                data.append("] [");
                c.debugInfo(pass, program, mesh, log, debug);
                data.append("]");
            }

            data.append("] ");
        }
    }
}
