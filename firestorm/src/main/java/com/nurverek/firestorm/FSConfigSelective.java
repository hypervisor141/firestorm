package com.nurverek.firestorm;

import vanguard.VLDebug;
import vanguard.VLListType;

public class FSConfigSelective extends FSConfigLocated{

    private final VLListType<FSConfig> configs;
    private FSConfig active;
    private int glslsize;

    public FSConfigSelective(VLListType<FSConfig> configs){
        this.configs = configs;
        glslsize = 0;
    }

    public FSConfigSelective(int size, int resizer){
        this.configs = new VLListType<>(size, resizer);
        glslsize = 0;
    }

    @Override
    public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
        active.location(location());
        active.configure(program, mesh, meshindex, passindex);
    }

    @Override
    public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
        active.location(location());
        active.configureDebug(program, mesh, meshindex, passindex);
    }

    @Override
    protected void notifyProgramBuilt(FSP program){
        super.notifyProgramBuilt(program);

        int size = configs.size();

        for(int i = 0; i < size; i++){
            configs.get(i).notifyProgramBuilt(program);
        }
    }

    public void activate(int index){
        active = configs.get(index);
        glslsize = active.getGLSLSize();
    }

    public VLListType<FSConfig> configs(){
        return configs;
    }

    public FSConfig active(){
        return active;
    }

    @Override
    public int getGLSLSize(){
        return glslsize;
    }

    @Override
    public void debugInfo(FSP program, FSMesh mesh, int debug){
        super.debugInfo(program, mesh, debug);

        VLDebug.append("activeConfig[");
        VLDebug.append(active == null ? "NULL" : active.getClass().getSimpleName());
        VLDebug.append("] [");

        if(active == null){
            VLDebug.append("NULL");

        }else{
            active.debugInfo(program, mesh, debug);
        }

        VLDebug.append("] configs[  ");
        int size = configs.size();

        for(int i = 0; i < size; i++){
            configs.get(i).debugInfo(program, mesh, debug);
            VLDebug.append("  ");
        }

        VLDebug.append("] ");
    }
}
