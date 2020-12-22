package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListType;

public abstract class FSGScanner{

    protected FSGAssembler assembler;
    protected FSBufferLayout layout;
    protected FSGData datagroup;

    protected FSMesh mesh;
    protected String name;

    private VLListType<FSP> programs;

    private FSGScanner(FSGAssembler assembler, FSGData datagroup, FSMesh mesh, String name){
        this.mesh = mesh;
        this.datagroup = datagroup;
        this.assembler = assembler;
        this.name = name;
        this.layout = layout;

        programs = new VLListType<>(10, 20);
        layout = new FSBufferLayout(mesh, assembler);

        mesh.name(name);
    }

    protected abstract boolean scan(FSGAutomator automator, FSM.Data data);

    protected void buffer(){
        layout.buffer();
    }

    protected void bufferDebug(){
        layout.bufferDebug(this);
    }

    protected void populatePrograms(){
        int size = programs.size();

        for(int i = 0; i < size; i++){
            programs.get(i).addMesh(mesh);
        }
    }

    protected void debugInfo(){
        VLDebug.append("[");
        VLDebug.append(getClass().getSimpleName());
        VLDebug.append("] ");
        VLDebug.append("mesh[" + mesh.name + "] ");

        int size = mesh.size();
        VLArrayFloat[] data;
        VLArrayFloat array;
        int[] requirements = new int[FSG.ELEMENT_TOTAL_COUNT];

        if(mesh.indices != null){
            requirements[FSG.ELEMENT_INDEX] = mesh.indices.size();
        }

        for(int i = 0; i < size; i++){
            data = mesh.instance(i).data.elements;

            for(int i2 = 0; i2 < data.length; i2++){
                array = data[i2];

                if(array != null){
                    requirements[i2] += array.size();
                }
            }
        }

        VLDebug.append("storageRequirements[");

        if(assembler.INSTANCE_SHARE_POSITIONS){
            requirements[FSG.ELEMENT_POSITION] /= size;
        }
        if(assembler.INSTANCE_SHARE_COLORS){
            requirements[FSG.ELEMENT_COLOR] /= size;
        }
        if(assembler.INSTANCE_SHARE_TEXCOORDS){
            requirements[FSG.ELEMENT_TEXCOORD] /= size;
        }
        if(assembler.INSTANCE_SHARE_NORMALS){
            requirements[FSG.ELEMENT_NORMAL] /= size;
        }

        size = FSG.ELEMENT_NAMES.length;

        for(int i = 0; i < size; i++){
            VLDebug.append(FSG.ELEMENT_NAMES[i]);
            VLDebug.append("[");
            VLDebug.append(requirements[i]);

            if(i < size - 1){
                VLDebug.append("] ");
            }
        }

        VLDebug.append("]]\n");
    }

    public static class Singular extends FSGScanner{

        protected Singular(FSGAssembler assembler, FSGData datagroup, String name, int drawmode){
            super(assembler, datagroup, new FSMesh(drawmode, 1, 0), name);
        }

        @Override
        protected boolean scan(FSGAutomator automator, FSM.Data fsm){
            if(fsm.name.equalsIgnoreCase(name)){
                if(assembler.LOAD_INDICES){
                    mesh.indices(new VLArrayShort(fsm.indices.array()));
                    assembler.buildFirst(this, fsm);

                    if(assembler.SYNC_INDICES_AND_BUFFER){
                        assembler.buffersteps[FSG.ELEMENT_INDEX] = FSGAssembler.BUFFER_SYNC;
                    }

                }else{
                    assembler.buildFirst(this, fsm);
                }

                return true;
            }

            return false;
        }
    }

    public static class Instanced extends FSGScanner{

        protected Instanced(FSGAssembler assembler, FSGData data, String prefixname, int drawmode, int estimatedsize){
            super(assembler, data, new FSMesh(drawmode, estimatedsize, (int)Math.ceil(estimatedsize / 2f)), prefixname);
        }

        @Override
        protected boolean scan(FSGAutomator automator, FSM.Data fsm){
            if(fsm.name.contains(name)){
                if(assembler.LOAD_INDICES && mesh.indices == null){
                    mesh.indices(new VLArrayShort(fsm.indices.array()));
                    assembler.buildFirst(this, fsm);

                    if(assembler.SYNC_INDICES_AND_BUFFER){
                        assembler.buffersteps[FSG.ELEMENT_INDEX] = FSGAssembler.BUFFER_SYNC;
                    }

                }else{
                    assembler.buildRest(this, fsm);
                }

                return true;
            }

            return false;
        }
    }
}
