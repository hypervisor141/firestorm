package com.nurverek.firestorm;

import com.nurverek.vanguard.VLArrayFloat;
import com.nurverek.vanguard.VLArrayShort;
import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListType;

public abstract class FSGScanner{

    protected FSGBluePrint blueprint;
    protected FSGAssembler assembler;
    protected FSBufferLayout layout;

    protected FSMesh mesh;
    protected String name;

    protected FSGScanner(FSGBluePrint blueprint, FSGAssembler assembler, String name){
        this.blueprint = blueprint;
        this.assembler = assembler;
        this.name = name;

        mesh = new FSMesh();
        mesh.name(name);
    }

    protected abstract boolean scan(FSGAutomator automator, FSM.Data data);

    protected void buffer(){
        layout.buffer(assembler);
    }

    protected void bufferDebug(){
        layout.bufferDebug(assembler);
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

        public Singular(FSGBluePrint blueprint, FSGAssembler assembler, String name, int drawmode){
            super(blueprint, assembler, name);
            mesh.initialize(drawmode, 1, 0);
        }

        @Override
        protected boolean scan(FSGAutomator automator, FSM.Data fsm){
            if(fsm.name.equalsIgnoreCase(name)){
                FSInstance instance = new FSInstance();
                mesh.addInstance(instance);

                blueprint.adjustPreAssembly(mesh, instance);

                if(assembler.LOAD_INDICES){
                    mesh.indices(new VLArrayShort(fsm.indices.array()));
                    assembler.buildFirst(instance, this, fsm);

                    if(assembler.SYNC_INDICES_AND_BUFFER){
                        assembler.buffersteps[FSG.ELEMENT_INDEX] = FSGAssembler.BUFFER_SYNC;
                    }

                }else{
                    assembler.buildFirst(instance, this, fsm);
                }

                return true;
            }

            return false;
        }
    }

    public static class Instanced extends FSGScanner{

        public Instanced(FSGBluePrint blueprint, FSGAssembler assembler, String prefixname, int drawmode, int estimatedsize){
            super(blueprint, assembler, prefixname);
            mesh.initialize(drawmode, estimatedsize, (int)Math.ceil(estimatedsize / 2f));
        }

        @Override
        protected boolean scan(FSGAutomator automator, FSM.Data fsm){
            if(fsm.name.contains(name)){
                FSInstance instance = new FSInstance();
                mesh.addInstance(instance);

                blueprint.adjustPreAssembly(mesh, instance);

                if(assembler.LOAD_INDICES && mesh.indices == null){
                    mesh.indices(new VLArrayShort(fsm.indices.array()));
                    assembler.buildFirst(instance, this, fsm);

                    if(assembler.SYNC_INDICES_AND_BUFFER){
                        assembler.buffersteps[FSG.ELEMENT_INDEX] = FSGAssembler.BUFFER_SYNC;
                    }

                }else{
                    assembler.buildRest(instance, this, fsm);
                }

                return true;
            }

            return false;
        }
    }
}
