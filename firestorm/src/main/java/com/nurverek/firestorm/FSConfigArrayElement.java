package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArray;
import com.nurverek.vanguard.VLDebug;

public abstract class FSConfigArrayElement <TYPE extends VLArray> extends FSConfigArray<TYPE>{

    private int instance;
    private int element;

    public FSConfigArrayElement(Policy policy, int element, int instance, int offset, int count){
        super(policy, null, offset, count);

        this.element = element;
        this.instance = instance;
    }

    public FSConfigArrayElement(int element, int instance, int offset, int count){
        super(null, offset, count);

        this.element = element;
        this.instance = instance;
    }

    public final void set(int instance, int element){
        this.element = element;
        this.instance = instance;
    }

    public final int element(){
        return element;
    }

    public final int instance(){
        return instance;
    }

    @Override
    public void configure(FSP program, FSMesh mesh, int meshindex, int passindex){
        array = (TYPE)mesh.get(instance).element(element);
    }

    @Override
    public void debugInfo(FSP program, FSMesh mesh, int debug){
        VLDebug.append("] instance[");
        VLDebug.append(instance);
        VLDebug.append("] element[");
        VLDebug.append(FSGenerator.ELEMENT_NAMES[element]);
        VLDebug.append("] array[");

        if(array == null){
            mesh.get(instance).element(element).stringify(VLDebug.get(), null);

        }else{
            array.stringify(VLDebug.get(), null);
        }

        VLDebug.append("]");
    }
}
