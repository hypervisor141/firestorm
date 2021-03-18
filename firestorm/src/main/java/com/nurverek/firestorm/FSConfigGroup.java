package com.nurverek.firestorm;

import vanguard.VLDebug;
import vanguard.VLListType;

public class FSConfigGroup extends FSConfig{

    public VLListType<FSConfig> configs;

    public FSConfigGroup(Policy policy, int capacity, int resizer){
        super(policy);
        configs = new VLListType<>(capacity, resizer);
    }

    public FSConfigGroup(Policy policy){
        super(policy);
    }

    public FSConfigGroup(){

    }

    @Override
    protected void notifyProgramBuilt(FSP program){
        int size = configs.size();

        for(int i = 0; i < size; i++){
            configs.get(i).notifyProgramBuilt(program);
        }
    }

    @Override
    public final void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
        int size = configs.size();

        for(int i = 0; i < size; i++){
            configs.get(i).configure(program, mesh, meshindex, passindex);
        }
    }

    @Override
    public final void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
        String classname = getClass().getSimpleName();
        int size = configs.size();

        VLDebug.append("ENTERING [");
        VLDebug.append(classname);
        VLDebug.append("] configs[");
        VLDebug.append(size);
        VLDebug.append("]\n");

        for(int i = 0; i < size; i++){
            VLDebug.append("[");
            VLDebug.append(i + 1);
            VLDebug.append("/");
            VLDebug.append(size);
            VLDebug.append("] ");

            configs.get(i).configureDebug(program, mesh, meshindex, passindex);
        }

        VLDebug.append("EXITING [");
        VLDebug.append(classname);
        VLDebug.append("]\n");
    }

    @Override
    public int getGLSLSize(){
        return 0;
    }

    @Override
    public void debugInfo(FSP program, FSMesh mesh, int debug){
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
                c.debugInfo(program, mesh, debug);
                data.append("]");
            }

            data.append("] ");
        }
    }
}
