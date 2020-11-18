package com.nurverek.firestorm;

import com.nurverek.vanguard.VLDebug;

public class FSConfigDynamicSelective extends FSConfigLocated {

    private FSConfigSelective config;
    private int targetindex;

    public FSConfigDynamicSelective(FSConfigSelective config, int targetindex){
        this.config = config;
        this.targetindex = targetindex;
    
        config.activate(targetindex);
    }

    @Override
    protected void programBuilt(FSP program){
        super.programBuilt(program);
        config.programBuilt(program);
    }

    @Override
    public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
        config.location(location());
        config.activate(targetindex);
        config.configure(program, mesh, meshindex, passindex);
    }

    @Override
    public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
        appendDebugHeader(program, mesh);

        config.location(location());
        config.activate(targetindex);
        config.configureDebug(program, mesh, meshindex, passindex);
    }

    @Override
    public int getGLSLSize(){
        return config.configs().get(targetindex).getGLSLSize();
    }

    @Override
    public void debugInfo(FSP program, FSMesh mesh, int debug){
        super.debugInfo(program, mesh, debug);

        VLDebug.append(" [");
        VLDebug.append(config.getClass().getSimpleName());
        VLDebug.append("] targetIndex[");
        VLDebug.append(targetindex);
        VLDebug.append("]");
    }
}
