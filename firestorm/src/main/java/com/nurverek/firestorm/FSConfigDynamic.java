package com.nurverek.firestorm;

import vanguard.VLDebug;

public class FSConfigDynamic<TYPE extends FSConfig> extends FSConfigLocated{

    private TYPE config;
    private final int glslsize;

    public FSConfigDynamic(Policy policy, TYPE config){
        super(policy);

        this.config = config;
        glslsize = config.getGLSLSize();
    }

    public FSConfigDynamic(TYPE config){
        this.config = config;
        glslsize = config.getGLSLSize();
    }

    public FSConfigDynamic(int glslsize){
        this.glslsize = glslsize;
    }

    @Override
    protected void notifyProgramBuilt(FSP program){
        config.notifyProgramBuilt(program);
    }

    @Override
    public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
        config.location(location);
        config.configure(program, mesh, meshindex, passindex);
    }

    @Override
    public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
        appendDebugHeader(program, mesh);

        config.location(location);
        config.configureDebug(program, mesh, meshindex, passindex);
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
    public void debugInfo(FSP program, FSMesh mesh, int debug){
        super.debugInfo(program, mesh, debug);

        VLDebug.append("config[");
        VLDebug.append(config.getClass().getSimpleName());
        VLDebug.append("] [ ");
        config.debugInfo(program, mesh, debug);
        VLDebug.append(" ] ");
    }
}
