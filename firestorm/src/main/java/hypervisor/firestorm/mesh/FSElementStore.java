package hypervisor.firestorm.mesh;

import hypervisor.vanguard.list.arraybacked.VLListType;
import hypervisor.vanguard.utils.VLCopyable;

public final class FSElementStore implements VLCopyable<FSElementStore>{

    protected VLListType<FSElement<?, ?>>[] vault;
    protected int[] active;

    protected FSElementStore(int size){
        vault = new VLListType[size];
        active = new int[size];
    }

    public FSElementStore(FSElementStore src, long flags){
        copy(src, flags);
    }

    protected FSElementStore(){

    }

    public void allocateElement(int element, int capacity, int resizeoverhead){
        vault[element] = new VLListType<>(capacity, resizeoverhead);
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
                    vault[i] = list.duplicate(FLAG_CUSTOM | VLListType.FLAG_DUPLICATE_ARRAY_FULLY);
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
