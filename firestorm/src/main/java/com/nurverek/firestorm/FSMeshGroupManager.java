package com.nurverek.firestorm;

import vanguard.VLListType;

public abstract class FSMeshGroupManager extends FSMeshGroup{

    public FSMeshGroupManager(int capacity, int resizer){
        super(capacity, resizer);
    }

    protected FSMeshGroupManager(){

    }

    public abstract void register(FSAutomator automator, FSRGlobal global);
}
