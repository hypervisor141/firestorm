package com.nurverek.firestorm;

import vanguard.VLLog;

public abstract class FSConfig{

    public static final Mode MODE_ALWAYS = new Mode(){

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configure(pass, program, mesh, meshindex, passindex);
        }

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
            self.configureDebug(pass, program, mesh, meshindex, passindex, log, debug);
        }
    };
    public static final Mode MODE_ONETIME = new Mode(){

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configure(pass, program, mesh, meshindex, passindex);
            self.mode = MODE_DISABLED;
        }

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
            self.configureDebug(pass, program, mesh, meshindex, passindex, log, debug);
            self.mode = MODE_DISABLED;
        }
    };
    public static final Mode MODE_DISABLED = new Mode(){

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){}

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){}
    };

    private Mode mode;

    public FSConfig(Mode mode){
        this.mode = mode;
    }

    public void location(int location){ }

    public int location(){
        return -Integer.MAX_VALUE;
    }

    public Mode mode(){
        return mode;
    }

    public void mode(Mode mode){
        this.mode = mode;
    }

    public abstract int getGLSLSize();

    public void run(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
        mode.configure(pass, this, program, mesh, meshindex, passindex);
    }

    public void runDebug(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
        mode.configureDebug(pass, this, program, mesh, meshindex, passindex, log, debug);
    }

    protected abstract void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex);

    protected void configureDebug(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
        try{
            printDebugHeader(pass, program, mesh, log, debug);

            configure(pass, program, mesh, meshindex, passindex);
            FSTools.checkGLError();

        }catch(Exception ex){
            log.append("[FAILED]");
            log.printError();

            throw new RuntimeException(ex);
        }
    }

    protected void notifyProgramBuilt(FSP program){}

    protected void printDebugHeader(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
        if(log != null){
            String classname = getClass().getSimpleName();

            log.append("[");
            log.append(classname == "" ? "Anonymous" : classname);
            log.append("]");

            if(program.debug >= FSControl.DEBUG_FULL){
                log.append(" [");
                debugInfo(pass, program, mesh, log, debug);
                log.append("]\n");

            }else{
                log.append("\n");
            }
        }
    }

    public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
        log.append("GLSLSize[");
        log.append(getGLSLSize());
        log.append("] ");
    }

    public interface Mode{

        void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex);
        void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug);
    }
}
