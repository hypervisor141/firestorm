package hypervisor.firestorm.engine;

import android.content.Context;

import hypervisor.firestorm.mesh.FSTypeRender;
import hypervisor.vanguard.list.VLListType;

public abstract class FSHub<GLOBAL extends FSGlobal>{

    VLListType<FSTypeRender> entries;

    public FSHub(int size, int resizer){
        entries = new VLListType<>(size, resizer);
    }

    public void initialize(Context context){
        assemble(context, (GLOBAL)FSGlobal.get(), entries);
    }

    protected abstract void assemble(Context context, GLOBAL global, VLListType<FSTypeRender> entries);

    protected void paused(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).paused();
        }
    }

    protected void resumed(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).resumed();
        }
    }

    protected void destroy(){
        int size = entries.size();

        for(int i = 0; i < size; i++){
            entries.get(i).destroy();
        }

        entries = null;
    }
}