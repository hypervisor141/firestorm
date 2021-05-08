package com.nurverek.firestorm;

import vanguard.VLLog;

public abstract class FSHScanner{

    protected FSHAssembler assembler;
    protected FSBufferMap map;
    protected FSP program;
    protected FSMesh mesh;
    protected String name;

    protected FSHScanner(FSMesh mesh, FSP program, FSBufferMap map, FSHAssembler assembler, String name){
        this.mesh = mesh;
        this.program = program;
        this.map = map;
        this.assembler = assembler;
        this.name = name.toLowerCase();
    }

    protected abstract boolean scan(FSAutomator automator, FSM.Data data);

    protected void bufferAndFinish(){
        map.buffer(mesh);
        program.meshes().add(mesh);
    }

    protected void scanComplete(){
        mesh.scanComplete();
    }

    protected void bufferComplete(){
        mesh.bufferComplete();
    }

    protected void accountForBufferSize(){
        map.accountFor(mesh);
    }

    protected void bufferDebugAndFinish(VLLog log){
        map.bufferDebug(mesh, log);
        program.meshes().add(mesh);
    }

    public static class Singular extends FSHScanner{

        public Singular(FSMesh mesh, FSP program, FSBufferMap map, FSHAssembler assembler, String name, int drawmode){
            super(mesh, program, map, assembler, name);
            mesh.initialize(drawmode, 1, 0);
        }

        @Override
        protected boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.equalsIgnoreCase(name)){
                mesh.name(name);

                FSInstance instance = mesh.generateInstance(data.name);

                assembler.buildFirst(instance, this, data);
                mesh.scanComplete(instance);

                return true;
            }

            return false;
        }
    }

    public static class Instanced extends FSHScanner{

        public Instanced(FSMesh mesh, FSP program, FSBufferMap map, FSHAssembler assembler, String prefixname, int drawmode, int estimatedsize){
            super(mesh, program, map, assembler, prefixname);
            mesh.initialize(drawmode, estimatedsize, (int)Math.ceil(estimatedsize / 2f));
        }

        @Override
        protected boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.contains(name)){
                mesh.name(name);

                FSInstance instance = mesh.generateInstance(data.name);

                if(mesh.size() == 1){
                    assembler.buildFirst(instance, this, data);

                }else{
                    assembler.buildRest(instance, this, data);
                }

                mesh.scanComplete(instance);

                return true;
            }

            return false;
        }
    }

    public static class InstancedCopy extends FSHScanner{

        private final int copycount;

        public InstancedCopy(FSMesh mesh, FSP program, FSBufferMap map, FSHAssembler assembler, String prefixname, int drawmode, int copycount){
            super(mesh, program, map, assembler, prefixname);

            this.copycount = copycount;
            mesh.initialize(drawmode, copycount, 0);
        }

        @Override
        protected boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.contains(name)){
                mesh.name(name);

                FSInstance instance = mesh.generateInstance(data.name);

                assembler.buildFirst(instance, this, data);
                mesh.scanComplete(instance);

                for(int i = 0; i < copycount; i++){
                    instance = mesh.generateInstance(data.name);

                    assembler.buildFirst(instance, this, data);
                    mesh.scanComplete(instance);
                }

                return true;
            }

            return false;
        }
    }
}
