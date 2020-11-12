package com.nurverek.firestorm;

import com.nurverek.vanguard.VLDebug;

public abstract class FSConfigLocated extends FSConfig{

    protected int location;

    public FSConfigLocated(Policy policy, int location){
        super(policy);
        this.location = location;
    }

    public FSConfigLocated(Policy policy){
        super(policy);
        location = -Integer.MAX_VALUE;
    }

    public FSConfigLocated(int location){
        this.location = location;
    }

    public FSConfigLocated(){
        location = -Integer.MAX_VALUE;
    }


    @Override
    public void location(int location){
        this.location = location;
    }

    @Override
    public int location(){
        return location;
    }

    @Override
    public void debugInfo(FSP program, FSMesh mesh, int debug){
        super.debugInfo(program, mesh, debug);

        VLDebug.append(" location[");
        VLDebug.append(location);
        VLDebug.append("]");
    }
}
