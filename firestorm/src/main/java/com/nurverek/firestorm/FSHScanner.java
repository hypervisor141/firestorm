package com.nurverek.firestorm;

import vanguard.VLLog;

public abstract class FSHScanner{

    protected FSHAssembler assembler;
    protected FSBufferTargets buffertarget;
    protected FSP program;
    protected FSMesh mesh;
    protected String name;

    protected FSHScanner(FSMesh mesh, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String name){
        this.mesh = mesh;
        this.program = program;
        this.buffertarget = buffertarget;
        this.assembler = assembler;
        this.name = name.toLowerCase();
    }

    abstract boolean scan(FSAutomator automator, FSM.Data data);

    void signalScanComplete(){
        mesh.scanComplete();
    }

    void signalBufferComplete(){
        mesh.bufferComplete();
    }

    void adjustBufferCapacity(){
        buffertarget.prepare(mesh);
    }

    void adjustBufferCapacityDebug(VLLog log){
        buffertarget.prepareDebug(mesh, log);
    }

    void bufferAndFinish(){
        buffertarget.buffer(mesh);
        program.meshes().add(mesh);
    }

    void bufferDebugAndFinish(VLLog log){
        buffertarget.bufferDebug(mesh, log);
        program.meshes().add(mesh);
    }

    void uploadBuffer(){
        buffertarget.upload();
    }

    public static class Singular extends FSHScanner{

        public Singular(FSMesh mesh, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String name, int drawmode){
            super(mesh, program, buffertarget, assembler, name);
            mesh.initialize(drawmode);
        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.equalsIgnoreCase(name)){
                if(mesh.size() > 0){
                    throw new RuntimeException("Found more than one instance for a singular scanner [" + mesh.name() + "]");
                }

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

        public Instanced(FSMesh mesh, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String substringname, int drawmode){
            super(mesh, program, buffertarget, assembler, substringname);
            mesh.initialize(drawmode);
        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.contains(name)){
                FSInstance instance = mesh.generateInstance(data.name);

                if(mesh.size() == 1){
                    mesh.name(name);
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

        public InstancedCopy(FSMesh mesh, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String prefixname, int drawmode, int copycount){
            super(mesh, program, buffertarget, assembler, prefixname);

            this.copycount = copycount;
            mesh.initialize(drawmode);
        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
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
