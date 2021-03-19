package com.nurverek.firestorm;

import vanguard.VLDebug;

public abstract class FSConfig{

    public static final Mode MODE_ALWAYS = new Mode(){

        @Override
        public void configure(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configure(program, mesh, meshindex, passindex);
        }

        @Override
        public void configureDebug(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configureDebug(program, mesh, meshindex, passindex);
        }
    };
    public static final Mode MODE_ONETIME = new Mode(){

        @Override
        public void configure(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configure(program, mesh, meshindex, passindex);
            self.mode = MODE_DISABLED;
        }

        @Override
        public void configureDebug(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configureDebug(program, mesh, meshindex, passindex);
            self.mode = MODE_DISABLED;
        }
    };
    public static final Mode MODE_DISABLED = new Mode(){

        @Override
        public void configure(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){}

        @Override
        public void configureDebug(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){}
    };

    private Mode mode;

    public FSConfig(Mode mode){
        this.mode = mode;
    }

    public void run(FSP program, FSMesh mesh, int meshindex, int passindex){
        mode.configure(this, program, mesh, meshindex, passindex);
    }

    public void runDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
        mode.configureDebug(this, program, mesh, meshindex, passindex);
    }

    protected abstract void configure(FSP program, FSMesh mesh, int meshindex, int passindex);

    protected void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
        try{
            printDebugHeader(program, mesh);

            configure(program, mesh, meshindex, passindex);
            FSTools.checkGLError();

        }catch(Exception ex){
            VLDebug.append("[FAILED]");
            VLDebug.printE();

            throw new RuntimeException(ex);
        }
    }

    protected void notifyProgramBuilt(FSP program){}

    public void location(int location){ }

    public int location(){
        return -Integer.MAX_VALUE;
    }

    public abstract int getGLSLSize();

    protected void printDebugHeader(FSP program, FSMesh mesh){
        String classname = getClass().getSimpleName();

        VLDebug.append("[");
        VLDebug.append(classname == "" ? "Anonymous" : classname);
        VLDebug.append("]");

        if(program.debug >= FSControl.DEBUG_FULL){
            VLDebug.append(" [");
            debugInfo(program, mesh, program.debug);
            VLDebug.append("]\n");

        }else{
            VLDebug.append("\n");
        }
    }

    public void debugInfo(FSP program, FSMesh mesh, int debug){
        VLDebug.append("GLSLSize[");
        VLDebug.append(getGLSLSize());
        VLDebug.append("] ");
    }

    public interface Mode{

        void configure(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex);
        void configureDebug(FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex);
    }
}
