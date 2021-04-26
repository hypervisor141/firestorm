package com.nurverek.firestorm;

import vanguard.VLListType;
import vanguard.VLLog;

public class FSConfigGroup extends FSConfig{

    public VLListType<FSConfig> configs;

    public FSConfigGroup(Mode mode, int capacity, int resizer){
        super(mode);
        configs = new VLListType<>(capacity, resizer);
    }

    public void add(FSConfig config){
        configs.add(config);
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

        for(int i = 0; i < size; i++){
            configs.get(i).run(pass, program, mesh, meshindex, passindex);
        }
    }

    @Override
    public final void configureDebug(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
        String classname = getClass().getSimpleName();
        int size = configs.size();

        log.append("ENTERING [");
        log.append(classname);
        log.append("] configs[");
        log.append(size);
        log.append("]\n");

        for(int i = 0; i < size; i++){
            log.append("[");
            log.append(i + 1);
            log.append("/");
            log.append(size);
            log.append("] ");

            configs.get(i).runDebug(pass, program, mesh, meshindex, passindex, log, debug);
        }

        log.append("EXITING [");
        log.append(classname);
        log.append("]\n");
    }

    @Override
    public int getGLSLSize(){
        return 0;
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

            if(program.debug >= FSControl.DEBUG_FULL){
                data.append("] [");
                c.debugInfo(pass, program, mesh, log, debug);
                data.append("]");
            }

            data.append("] ");
        }
    }
}
