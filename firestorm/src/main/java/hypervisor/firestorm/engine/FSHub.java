package hypervisor.firestorm.engine;

import android.content.Context;

public abstract class FSHub<GLOBAL extends FSGlobal>{

    public FSHub(){

    }

    public void initialize(Context context){
        assemble(context, (GLOBAL)FSGlobal.get());
    }

    protected abstract void assemble(Context context, GLOBAL global);
    protected abstract void paused();
    protected abstract void resumed();
    protected abstract void destroy();
}