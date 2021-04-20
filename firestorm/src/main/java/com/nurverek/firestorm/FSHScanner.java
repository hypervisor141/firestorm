package com.nurverek.firestorm;

import vanguard.VLArrayFloat;
import vanguard.VLArrayShort;
import vanguard.VLDebug;

public abstract class FSHScanner{

    protected FSHAssembler assembler;
    protected FSBufferLayout layout;
    protected FSP program;
    protected FSMesh<?> mesh;
    protected String name;

    protected FSHScanner(FSMesh<?> mesh, FSP program, FSBufferLayout layout, FSHAssembler assembler, String name){
        this.mesh = mesh;
        this.program = program;
        this.layout = layout;
        this.assembler = assembler;
        this.name = name.toLowerCase();
    }

    protected abstract boolean scan(FSHub.Automator automator, FSM.Data data);

    protected void bufferAndFinish(){
        layout.buffer(mesh);
        program.meshes().add(mesh);
    }

    protected void scanComplete(){
        mesh.scanComplete();
    }

    protected void bufferComplete(){
        mesh.bufferComplete();
    }

    protected void accountForBufferSize(){
        layout.accountFor(mesh);
    }

    protected void bufferDebugAndFinish(){
        layout.bufferDebug(mesh);
        program.meshes().add(mesh);
    }

    protected void debugInfo(){
        VLDebug.append("[");
        VLDebug.append(getClass().getSimpleName());
        VLDebug.append("] ");
        VLDebug.append("mesh[" + mesh.name + "] ");

        int size = mesh.size();
        VLArrayFloat[] data;
        VLArrayFloat array;
        int[] requirements = new int[FSHub.ELEMENT_TOTAL_COUNT];

        if(mesh.indices != null){
            requirements[FSHub.ELEMENT_INDEX] = mesh.indices.size();
        }

        for(int i = 0; i < size; i++){
            data = mesh.get(i).data.elements;

            for(int i2 = 0; i2 < data.length; i2++){
                array = data[i2];

                if(array != null){
                    requirements[i2] += array.size();
                }
            }
        }

        VLDebug.append("storageRequirements[");

        if(assembler.INSTANCE_SHARE_POSITIONS){
            requirements[FSHub.ELEMENT_POSITION] /= size;
        }
        if(assembler.INSTANCE_SHARE_COLORS){
            requirements[FSHub.ELEMENT_COLOR] /= size;
        }
        if(assembler.INSTANCE_SHARE_TEXCOORDS){
            requirements[FSHub.ELEMENT_TEXCOORD] /= size;
        }
        if(assembler.INSTANCE_SHARE_NORMALS){
            requirements[FSHub.ELEMENT_NORMAL] /= size;
        }

        size = FSHub.ELEMENT_NAMES.length;

        for(int i = 0; i < size; i++){
            VLDebug.append(FSHub.ELEMENT_NAMES[i]);
            VLDebug.append("[");
            VLDebug.append(requirements[i]);

            if(i < size - 1){
                VLDebug.append("] ");
            }
        }

        VLDebug.append("]]\n");
    }

    public static class Singular extends FSHScanner{

        public Singular(FSMesh<?> mesh, FSP program, FSBufferLayout layout, FSHAssembler assembler, String name, int drawmode){
            super(mesh, program, layout, assembler, name);
            mesh.initialize(drawmode, 1, 0);
        }

        @Override
        protected boolean scan(FSHub.Automator automator, FSM.Data data){
            if(data.name.equalsIgnoreCase(name)){
                mesh.name(name);

                FSInstance instance = mesh.generateInstance(data.name);

                if(assembler.LOAD_INDICES){
                    mesh.indices(new VLArrayShort(data.indices.array()));
                }

                assembler.buildFirst(instance, this, data);

                return true;
            }

            return false;
        }
    }

    public static class Instanced extends FSHScanner{

        public Instanced(FSMesh<?> mesh, FSP program, FSBufferLayout layout, FSHAssembler assembler, String prefixname, int drawmode, int estimatedsize){
            super(mesh, program, layout, assembler, prefixname);
            mesh.initialize(drawmode, estimatedsize, (int)Math.ceil(estimatedsize / 2f));
        }

        @Override
        protected boolean scan(FSHub.Automator automator, FSM.Data data){
            if(data.name.contains(name)){
                mesh.name(name);

                FSInstance instance = mesh.generateInstance(data.name);

                if(assembler.LOAD_INDICES && mesh.indices == null){
                    mesh.indices(new VLArrayShort(data.indices.array()));
                    assembler.buildFirst(instance, this, data);

                }else{
                    assembler.buildRest(instance, this, data);
                }

                return true;
            }

            return false;
        }
    }

    public static class InstancedCopy extends FSHScanner{

        private final int copycount;

        public InstancedCopy(FSMesh<?> mesh, FSP program, FSBufferLayout layout, FSHAssembler assembler, String prefixname, int drawmode, int copycount){
            super(mesh, program, layout, assembler, prefixname);

            this.copycount = copycount;
            mesh.initialize(drawmode, copycount, 0);
        }

        @Override
        protected boolean scan(FSHub.Automator automator, FSM.Data data){
            if(data.name.contains(name)){
                mesh.name(name);

                FSInstance instance = mesh.generateInstance(data.name);

                if(assembler.LOAD_INDICES && mesh.indices == null){
                    mesh.indices(new VLArrayShort(data.indices.array()));
                    assembler.buildFirst(instance, this, data);

                    for(int i = 0; i < copycount; i++){
                        instance = mesh.generateInstance(data.name);

                        assembler.buildFirst(instance, this, data);
                    }
                }

                return true;
            }

            return false;
        }
    }
}
