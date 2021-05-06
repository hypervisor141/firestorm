package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;
import vanguard.VLLog;

public abstract class FSConfig implements VLCopyable<FSConfig>{

    public static final Mode MODE_FULLTIME = new Mode(){

        private static final String NAME = "FULLTIME";

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){
            self.configure(pass, program, mesh, meshindex, passindex);
        }

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
            self.configureDebug(pass, program, mesh, meshindex, passindex, log, debug);
        }

        @Override
        public String getModeName(){
            return NAME;
        }

        @Override
        public void copy(Mode src, long flags){}

        @Override
        public Mode duplicate(long flags){
            return this;
        }
    };
    public static final Mode MODE_ONETIME = new Mode(){

        private static final String NAME = "ONETIME";

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

        @Override
        public String getModeName(){
            return NAME;
        }

        @Override
        public void copy(Mode src, long flags){}

        @Override
        public Mode duplicate(long flags){
            return this;
        }
    };
    public static final Mode MODE_DISABLED = new Mode(){

        private static final String NAME = "DISABLED";

        @Override
        public void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex){}

        @Override
        public void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug){
            self.printHeader(log);
        }

        @Override
        public String getModeName(){
            return NAME;
        }

        @Override
        public void copy(Mode src, long flags){}

        @Override
        public Mode duplicate(long flags){
            return this;
        }
    };

    private Mode mode;

    public FSConfig(Mode mode){
        this.mode = mode;
    }

    protected FSConfig(){

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
            printDebugInfo(pass, program, mesh, log, debug);

            configure(pass, program, mesh, meshindex, passindex);
            FSTools.checkGLError();

        }catch(Exception ex){
            log.append("[FAILED]");
            log.printError();

            throw new RuntimeException(ex);
        }
    }

    protected void notifyProgramBuilt(FSP program){}

    protected void printHeader(VLLog log){
        if(log != null){
            String classname = getClass().getSimpleName();

            log.append("[");
            log.append(mode.getModeName());
            log.append("] [");
            log.append(classname.equals("") ? "Anonymous" : classname);
            log.append("]");
        }
    }

    protected void printDebugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
        if(log != null){
            printHeader(log);

            if(debug >= FSControl.DEBUG_FULL){
                log.append(" [");

                debugInfo(pass, program, mesh, log, debug);

                log.append("]\n");

            }else{
                log.append("\n");
            }
        }
    }

    @Override
    public void copy(FSConfig src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            mode = src.mode;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            mode = src.mode.duplicate(FLAG_DUPLICATE);

        }else{
            Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public abstract FSConfig duplicate(long flags);

    public void debugInfo(FSRPass pass, FSP program, FSMesh mesh, VLLog log, int debug){
        log.append("GLSLSize[");
        log.append(getGLSLSize());
        log.append("] ");
    }

    public interface Mode extends VLCopyable<Mode>{

        void configure(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex);
        void configureDebug(FSRPass pass, FSConfig self, FSP program, FSMesh mesh, int meshindex, int passindex, VLLog log, int debug);
        String getModeName();
    }
}
