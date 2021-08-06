package hypervisor.firestorm.engine;

import android.content.Context;

import hypervisor.firestorm.mesh.FSMeshGroup;
import hypervisor.firestorm.mesh.FSTypeRenderGroup;
import hypervisor.vanguard.list.VLListType;

public abstract class FSHub<GLOBAL extends FSGlobal>{

    public FSMeshGroup<FSTypeRenderGroup<?>> root;

    public FSHub(String rootname, int capacity, int resizeoverhead){
        root = new FSMeshGroup<>(rootname, capacity, resizeoverhead);
    }

    public void initialize(Context context){
        assemble(context, (GLOBAL)FSGlobal.get(), root);
    }

    protected abstract void assemble(Context context, GLOBAL global, FSMeshGroup<FSTypeRenderGroup<?>> root);

    protected void paused(){
        root.paused();
    }

    protected void resumed(){
        root.resumed();
    }

    protected void destroy(){
        root.destroy();
    }
}