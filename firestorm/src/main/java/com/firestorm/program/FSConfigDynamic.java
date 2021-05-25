package com.firestorm.program;

import com.firestorm.engine.FSRPass;
import com.firestorm.mesh.FSInstance;
import com.firestorm.mesh.FSMesh;

import vanguard.utils.VLCopyable;
import vanguard.utils.VLLog;

public class FSConfigDynamic<TYPE extends FSConfig> extends FSConfigLocated{

    protected TYPE config;
    protected int glslsize;

    public FSConfigDynamic(Mode mode, TYPE config){
        super(mode);

        this.config = config;
        glslsize = config.getGLSLSize();
    }

    public FSConfigDynamic(Mode mode, int glslsize){
        super(mode);
        this.glslsize = glslsize;
    }

    public FSConfigDynamic(FSConfigDynamic<TYPE> src, long flags){
        super(null);
        copy(src, flags);
    }

    protected FSConfigDynamic(){

    }

    @Override
    protected void notifyProgramBuilt(FSP program){
        config.notifyProgramBuilt(program);
    }

    @Override
    public void configure(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex){
        config.location(location);
        config.run(pass, program, mesh, meshindex, passindex);
    }

    @Override
    public void configureDebug(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, int meshindex, int passindex, VLLog log, int debug){
        printDebugInfo(pass, program, mesh, log, debug);

        config.location(location);
        config.runDebug(pass, program, mesh, meshindex, passindex, log, debug);
    }

    public void config(TYPE config){
        this.config = config;
    }

    public TYPE config(){
        return config;
    }

    @Override
    public int getGLSLSize(){
        return glslsize;
    }

    @Override
    public void copy(FSConfig src, long flags){
        super.copy(src, flags);

        FSConfigDynamic<TYPE> target = (FSConfigDynamic<TYPE>)src;

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            config = target.config;
            glslsize = target.glslsize;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            config = (TYPE)target.config.duplicate(FLAG_DUPLICATE);
            glslsize = target.glslsize;

        }else{
            VLCopyable.Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public FSConfigDynamic<TYPE> duplicate(long flags){
        return new FSConfigDynamic<>(this, flags);
    }

    @Override
    public void debugInfo(FSRPass pass, FSP program, FSMesh<FSInstance> mesh, VLLog log, int debug){
        super.debugInfo(pass, program, mesh, log, debug);

        log.append("config[");
        log.append(config.getClass().getSimpleName());
        log.append("] [ ");

        config.debugInfo(pass, program, mesh, log, debug);

        log.append(" ] ");
    }
}
