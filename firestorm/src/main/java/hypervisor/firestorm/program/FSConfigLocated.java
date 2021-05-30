package hypervisor.firestorm.program;

import hypervisor.firestorm.engine.FSRPass;
import hypervisor.firestorm.mesh.FSTypeInstance;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.vanguard.utils.VLLog;

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

    protected FSConfigLocated(){

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
    public void copy(FSConfig src, long flags){
        super.copy(src, flags);
        location = ((FSConfigLocated)src).location;
    }

    @Override
    public abstract FSConfigLocated duplicate(long flags);

    @Override
    public void debugInfo(FSRPass pass, FSP program, FSTypeMesh<FSTypeInstance> mesh, VLLog log, int debug){
        super.debugInfo(pass, program, mesh, log, debug);

        log.append(" location[");
        log.append(location);
        log.append("]");
    }
}
