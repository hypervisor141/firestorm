package hypervisor.firestorm.program;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.vanguard.array.VLArrayFloat;
import hypervisor.vanguard.utils.VLCopyable;

public abstract class FSLight implements VLCopyable<FSLight>{

    protected long id;

    public FSLight(){
        id = FSControl.generateUID();
    }

    public VLArrayFloat position(){
        return null;
    }

    public long id(){
        return id;
    }

    @Override
    public void copy(FSLight src, long flags){
        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            id = src.id;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            id = FSControl.generateUID();

        }else{
            Helper.throwMissingDefaultFlags();
        }
    }

    @Override
    public abstract FSLight duplicate(long flags);
}
