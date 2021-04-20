package com.nurverek.firestorm;

import vanguard.VLDebug;

public abstract class FSConfig{

    public static final Mode MODE_ALWAYS = new Mode(){

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configure(pass, program, mesh, meshindex, passindex);
        }

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configureDebug(pass, program, mesh, meshindex, passindex);
        }
    };
    public static final Mode MODE_ONETIME = new Mode(){

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configure(pass, program, mesh, meshindex, passindex);
            self.mode = MODE_DISABLED;
        }

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configureDebug(pass, program, mesh, meshindex, passindex);
            self.mode = MODE_DISABLED;
        }
    };
    public static final Mode MODE_DISABLED = new Mode(){

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){}

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){}
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

    public void runDebug(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
        mode.configureDebug(pass, this, program, mesh, meshindex, passindex);
    }

    protected abstract void configure(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex);

    protected void configureDebug(FSRPass pass, FSP program, FSMesh mesh, int meshindex, int passindex){
        try{
            printDebugHeader(pass, program, mesh);

            configure(pass, program, mesh, meshindex, passindex);
            FSTools.checkGLError();

        }catch(Exception ex){
            VLDebug.append("[FAILED]");
            VLDebug.printE();

            throw new RuntimeException(ex);
        }
    }

    protected void notifyProgramBuilt(FSP program){}

    protected void printDebugHeader(FSRPass pass, FSP program, FSMesh mesh){
        String classname = getClass().getSimpleName();

        VLDebug.append("[");
        VLDebug.append(classname == "" ? "Anonymous" : classname);
        VLDebug.append("]");

        if(program != null && program.debug >= FSControl.DEBUG_FULL){
            VLDebug.append(" [");
            debugInfo(pass, program, mesh, program.debug);
            VLDebug.append("]\n");

        }else{
            VLDebug.append("\n");
        }
    }

    public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, int debug){
        VLDebug.append("GLSLSize[");
        VLDebug.append(getGLSLSize());
        VLDebug.append("] ");
    }

    public interface Mode{

        void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex);
        void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex);
    }
}
