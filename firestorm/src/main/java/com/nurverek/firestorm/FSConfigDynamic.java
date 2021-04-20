package com.nurverek.firestorm;

import vanguard.VLDebug;

public class FSConfigDynamic<TYPE extends FSConfig> extends FSConfigLocated{

    private TYPE config;
    private final int glslsize;

    public FSConfigDynamic(Mode mode, TYPE config){
        super(mode);

        this.config = config;
        glslsize = config.getGLSLSize();
    }

    public FSConfigDynamic(Mode mode, int glslsize){
        super(mode);
        this.glslsize = glslsize;
    }

    @Override
    protected void notifyProgramBuilt(FSP program){
        config.notifyProgramBuilt(program);
    }

    @Override
    public void configure(FSRPass pass, FSP program, FSMesh<?> mesh, int meshindex, int passindex){
        config.location(location);
        config.run(pass, program, mesh, meshindex, passindex);
    }

    @Override
    public void configureDebug(FSRPass pass, FSP program, FSMesh<?> mesh, int meshindex, int passindex){
        printDebugHeader(pass, program, mesh);

        config.location(location);
        config.runDebug(pass, program, mesh, meshindex, passindex);
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
    public void debugInfo(FSRPass pass, FSP program, FSMesh<?> mesh, int debug){
        super.debugInfo(pass, program, mesh, debug);

        VLDebug.append("config[");
        VLDebug.append(config.getClass().getSimpleName());
        VLDebug.append("] [ ");

        config.debugInfo(pass, program, mesh, debug);

        VLDebug.append(" ] ");
    }
}
