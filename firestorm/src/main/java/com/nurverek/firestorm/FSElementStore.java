package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLBufferFloat;
import vanguard.VLBufferShort;
import vanguard.VLListType;

public final class FSElementStore{

    protected VLListType<FSElement<?, ?>>[] vault;
    protected FSElement<?, ?>[] active;

    protected FSElementStore(){
        vault = new VLListType[FSGlobal.COUNT];
        active = new FSElement<?, ?>[FSGlobal.COUNT];
    }

    public void allocateElement(int element, int capacity, int resizer){
        vault[element] = new VLListType<>(capacity, resizer);
    }

    public void activate(int element, int index){
        active[element] = vault[element].get(index);
    }

    public void add(int element, FSElement<?, ?> entry){
        vault[element].add(entry);
    }

    public FSElement<?, ?> active(int element){
        return active[element];
    }

    public FSElement<?, ?> get(int element, int index){
        return vault[element].get(index);
    }

    public VLListType<FSElement<?, ?>> get(int element){
        return vault[element];
    }

    public VLListType<FSElement<?,?>>[] get(){
        return vault;
    }

    public void remove(int element, int stateindex){
        vault[element].remove(stateindex);
    }

    public FSElement<?, ?> first(int element){
        return vault[element].get(0);
    }
}
