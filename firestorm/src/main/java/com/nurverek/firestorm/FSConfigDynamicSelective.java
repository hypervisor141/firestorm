package com.nurverek.firestorm;

import vanguard.VLDebug;

public class FSConfigDynamicSelective extends FSConfigLocated{

    private FSConfigSelective config;
    private int targetindex;

    public FSConfigDynamicSelective(Mode mode, FSConfigSelective config, int targetindex){
        super(mode);

        this.config = config;
        this.targetindex = targetindex;

        config.activate(targetindex);
    }

    public FSConfigDynamicSelective(Mode mode){
        super(mode);
    }

    public void config(FSConfigSelective config){
        this.config = config;
    }

    public void targetIndex(int index){
        targetindex = index;
        config.activate(targetindex);
    }

    public FSConfigSelective config(){
        return config;
    }

    public int targetIndex(){
        return targetindex;
    }

    @Override
    protected void notifyProgramBuilt(FSP program){
        config.notifyProgramBuilt(program);
    }

    @Override
    public void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
        config.location(location());
        config.activate(targetindex);
        config.run(pass, program, mesh, meshindex, passindex);
    }

    @Override
    public void configureDebug(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
        printDebugHeader(pass, program, mesh);

        config.location(location());
        config.activate(targetindex);
        config.runDebug(pass, program, mesh, meshindex, passindex);
    }

    @Override
    public int getGLSLSize(){
        return config.configs().get(targetindex).getGLSLSize();
    }

    @Override
    public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, int debug){
        super.debugInfo(pass, program, mesh, debug);

        VLDebug.append("[");
        VLDebug.append(config.getClass().getSimpleName());
        VLDebug.append("] targetIndex[");
        VLDebug.append(targetindex);
        VLDebug.append("] ");
    }
}
