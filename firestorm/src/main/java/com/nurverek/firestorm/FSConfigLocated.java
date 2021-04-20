package com.nurverek.firestorm;

import vanguard.VLDebug;

public abstract class FSConfigLocated extends FSConfig{

    protected int location;

    public FSConfigLocated(Mode mode, int location){
        super(mode);
        this.location = location;
    }

    public FSConfigLocated(Mode mode){
        super(mode);
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
    public void debugInfo(FSRPass pass, FSP program, FSMesh<?> mesh, int debug){
        super.debugInfo(pass, program, mesh, debug);

        VLDebug.append("location[");
        VLDebug.append(location);
        VLDebug.append("] ");
    }
}
