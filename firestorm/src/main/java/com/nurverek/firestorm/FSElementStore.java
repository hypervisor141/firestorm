package com.nurverek.firestorm;

import vanguard.VLCopyable;
import vanguard.VLListType;

public final class FSElementStore implements VLCopyable<FSElementStore>{

    protected VLListType<FSElement<?, ?>>[] vault;
    protected int[] active;

    protected FSElementStore(){
        vault = new VLListType[FSElementRegisry.COUNT];
        active = new int[FSElementRegisry.COUNT];
    }

    public FSElementStore(FSElementStore src, long flags){
        copy(src, flags);
    }

    public void allocateElement(int element, int capacity, int resizer){
        vault[element] = new VLListType<>(capacity, resizer);
    }

    public void activate(int element, int index){
        active[element] = index;
    }

    public void add(int element, FSElement<?, ?> entry){
        vault[element].add(entry);
    }

    public FSElement<?, ?> active(int element){
        return vault[element].get(active[element]);
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

    public int size(int element){
        return vault[element].size();
    }

    @Override
    public void copy(FSElementStore src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            vault = src.vault;
            active = src.active;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            VLListType<FSElement<?, ?>>[] srcvault = src.vault;
            int size = srcvault.length;

            vault = new VLListType[size];

            for(int i = 0; i < size; i++){
                VLListType<FSElement<?, ?>> list = srcvault[i];

                if(list != null){
                    vault[i] = list.duplicate(FLAG_CUSTOM | VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
                }
            }

            active = src.active.clone();

        }else{
            Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public FSElementStore duplicate(long flags){
        return new FSElementStore(this, flags);
    }
}
