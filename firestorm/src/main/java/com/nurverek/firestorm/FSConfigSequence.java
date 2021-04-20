package com.nurverek.firestorm;

import vanguard.VLDebug;
import vanguard.VLListType;

public class FSConfigSequence extends FSConfigLocated{

    public VLListType<FSConfig> configs;
    private int glslsize;

    public FSConfigSequence(Mode mode, VLListType<FSConfig> configs){
        super(mode);
        update(configs);
    }

    public FSConfigSequence(Mode mode, int glslsize){
        super(mode);
        this.glslsize = glslsize;
    }

    public FSConfigSequence(Mode mode){
        super(mode);
    }

    public void update(VLListType<FSConfig> stages){
        this.configs = stages;

        updateGLSLSize();
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
    public final void configure(FSRPass pass, FSP program, FSMesh<?> mesh, int meshindex, int passindex){
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
    public final void configureDebug(FSRPass pass, FSP program, FSMesh<?> mesh, int meshindex, int passindex){
        String classname = getClass().getSimpleName();
        int size = configs.size();
        FSConfig c;

        int loc = location;

        VLDebug.append("ENTERING [");
        VLDebug.append(classname);
        VLDebug.append("] configs[");
        VLDebug.append(size);
        VLDebug.append("] location[");
        VLDebug.append(loc);
        VLDebug.append("] GLSLSize[");
        VLDebug.append(glslsize);
        VLDebug.append("]\n");

        for(int i = 0; i < size; i++){
            VLDebug.append("[");
            VLDebug.append(i + 1);
            VLDebug.append("/");
            VLDebug.append(size);
            VLDebug.append("] ");

            c = configs.get(i);
            c.location(loc);
            c.runDebug(pass, program, mesh, meshindex, passindex);

            loc += c.getGLSLSize();
        }

        VLDebug.append("EXITING [");
        VLDebug.append(classname);
        VLDebug.append("]\n");
    }

    @Override
    public int getGLSLSize(){
        return glslsize;
    }

    @Override
    public void debugInfo(FSRPass pass, FSP program, FSMesh<?> mesh, int debug){
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
                c.debugInfo(pass, program, mesh, debug);
                data.append("]");
            }

            data.append("] ");
        }
    }
}
