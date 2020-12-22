package com.nurverek.firestorm;

import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLSyncable;

public abstract class FSConfig extends VLSyncable{

    public static final Policy POLICY_ALWAYS = new Policy(){

        @Override
        protected void configure(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex){
            config.configure(program, mesh, meshindex, passindex);
        }

        @Override
        protected void configureDebug(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex){
            config.configureDebug(program, mesh, meshindex, passindex);
        }
    };
    public static final Policy POLICY_ONCE = new Policy(){

        @Override
        protected void configure(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex){
            config.configure(program, mesh, meshindex, passindex);
            config.policy(POLICY_IDLE);
        }

        @Override
        protected void configureDebug(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex){
            config.configureDebug(program, mesh, meshindex, passindex);
            config.policy(POLICY_IDLE);
        }
    };
    public static final Policy POLICY_IDLE = new Policy(){

        @Override
        protected void configure(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex){ }

        @Override
        protected void configureDebug(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex){ }
    };

    private Policy policy;

    public FSConfig(Policy policy){
        this.policy = policy;
    }

    public FSConfig(){
        policy = POLICY_ALWAYS;
    }

    public abstract void configure(FSP program, FSMesh mesh, int meshindex, int passindex);

    public void configureDebug(FSP program, FSMesh mesh, int meshindex, int passindex){
        try{
            appendDebugHeader(program, mesh);

            configure(program, mesh, meshindex, passindex);
            FSTools.checkGLError();

        }catch(Exception ex){
            VLDebug.append("[FAILED]");
            VLDebug.printE();

            throw new RuntimeException(ex);
        }
    }

    protected void programBuilt(FSP program){}

    public void policy(Policy policy){
        this.policy = policy;
    }

    public void location(int location){ }

    public Policy policy(){
        return policy;
    }

    public int location(){
        return -Integer.MAX_VALUE;
    }

    public abstract int getGLSLSize();

    protected void appendDebugHeader(FSP program, FSMesh mesh){
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

    public abstract static class Policy{

        protected abstract void configure(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex);
        protected abstract void configureDebug(FSConfig config, FSP program, FSMesh mesh, int meshindex, int passindex);
    }

    public static final class Definition extends VLSyncable.Definition<Object, FSConfig>{

        public Definition(FSConfig target){
            super(target);
        }

        @Override
        protected void sync(Object source, FSConfig target){
            if(target.policy != FSConfig.POLICY_ALWAYS){
                target.policy = FSConfig.POLICY_ONCE;
            }
        }
    }
}
