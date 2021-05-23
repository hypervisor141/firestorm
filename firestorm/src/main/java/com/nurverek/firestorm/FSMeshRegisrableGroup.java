package com.nurverek.firestorm;

import vanguard.VLListType;

public abstract class FSMeshRegisrableGroup extends FSMeshGroup{

    public FSMeshRegisrableGroup(int capacity, int resizer){
        super(capacity, resizer);
    }

    protected FSMeshRegisrableGroup(){

    }

    public abstract void register(FSAutomator automator, FSRGlobal global);
}
