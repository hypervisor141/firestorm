package com.nurverek.firestorm;

import vanguard.VLListType;

public abstract class FSBufferPool{

    protected VLListType<FSBufferTargets> targets;

    public FSBufferPool(int capacity, int resizer){
        targets = new VLListType<>(capacity, resizer);
        initialize(targets);
    }

    protected FSBufferTargets get(int index){
        return targets.get(index);
    }

    protected abstract void initialize(VLListType<FSBufferTargets> targets);
    protected abstract void destroy();
}
