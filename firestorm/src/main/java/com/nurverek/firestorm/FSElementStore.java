package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLBufferFloat;
import vanguard.VLBufferShort;
import vanguard.VLCopyable;
import vanguard.VLListType;

public final class FSElementStore implements VLCopyable<FSElementStore>{

    protected VLListType<FSElement<?, ?>>[] vault;
    protected FSElement<?, ?>[] active;

    protected FSElementStore(){
        vault = new VLListType[FSGlobal.COUNT];
        active = new FSElement<?, ?>[FSGlobal.COUNT];
    }

    public FSElementStore(FSElementStore src, long flags){
        copy(src, flags);
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
                if(srcvault[i] != null){
                    vault[i] = srcvault[i].duplicate(VLListType.FLAG_FORCE_DUPLICATE_ARRAY);
                }
            }

            FSElement<?, ?>[] srcactive = src.active;
            size = srcactive.length;

            active = new FSElement<?, ?>[size];

            for(int i = 0; i < size; i++){
                if(active[i] != null){
                    active[i] = active[i].duplicate(FLAG_DUPLICATE);
                }
            }

        }else if((flags & FLAG_CUSTOM) == FLAG_CUSTOM){
            Helper.throwCustomCopyNotSupported(flags);

        }else{
            Helper.throwMissingBaseFlags();
        }
    }

    @Override
    public FSElementStore duplicate(long flags){
        return new FSElementStore(this, flags);
    }
}
