package com.nurverek.firestorm;

import vanguard.VLLog;

public abstract class FSHScanner{

    protected FSHAssembler assembler;
    protected FSBufferTargets buffertarget;
    protected FSP program;
    protected FSMesh<?> target;
    protected String name;

    protected FSHScanner(FSMesh<?> target, FSP program, FSBufferTargets buffetarget, FSHAssembler assembler, String name){
        this.target = target;
        this.program = program;
        this.buffertarget = buffetarget;
        this.assembler = assembler;
        this.name = name.toLowerCase();
    }

    abstract boolean scan(FSAutomator automator, FSM.Data data);

    void signalScanComplete(){
        target.scanComplete();
    }

    void signalBuildComplete(){
        target.buildComplete();
    }

    void adjustBufferCapacity(){
        buffertarget.prepare(target);
    }

    void adjustBufferCapacityDebug(VLLog log){
        buffertarget.prepareDebug(target, log);
    }

    void bufferAndFinish(){
        buffertarget.buffer(target);
        program.meshes().add(target);
    }

    void bufferDebugAndFinish(VLLog log){
        buffertarget.bufferDebug(target, log);
        program.meshes().add(target);
    }

    void uploadBuffer(){
        buffertarget.upload();
    }

    public static class Singular extends FSHScanner{

        public Singular(FSMesh<?> target, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String name){
            super(target, program, buffertarget, assembler, name);
        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.equalsIgnoreCase(name)){
                if(target.size() > 0){
                    throw new RuntimeException("Found more than one instance for a singular scanner [" + target.name() + "]");
                }

                target.name(name);
                FSInstance instance = target.addNewInstance(data.name);

                assembler.buildFirst(instance, this, data);
                instance.scanComplete();

                return true;
            }

            return false;
        }
    }

    public static class Instanced extends FSHScanner{

        public Instanced(FSMesh<?> target, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String substringname){
            super(target, program, buffertarget, assembler, substringname);
        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.contains(name)){
                FSInstance instance = target.addNewInstance(data.name);

                if(target.size() == 1){
                    target.name(name);
                    assembler.buildFirst(instance, this, data);

                }else{
                    assembler.buildRest(instance, this, data);
                }

                instance.scanComplete();

                return true;
            }

            return false;
        }
    }

    public static class InstancedCopy extends FSHScanner{

        private final int copycount;

        public InstancedCopy(FSMesh<?> target, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String prefixname, int copycount){
            super(target, program, buffertarget, assembler, prefixname);
            this.copycount = copycount;
        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.contains(name)){
                target.name(name);
                FSInstance instance = target.addNewInstance(data.name);

                assembler.buildFirst(instance, this, data);
                instance.scanComplete();

                for(int i = 0; i < copycount; i++){
                    instance = target.addNewInstance(data.name);

                    assembler.buildFirst(instance, this, data);
                    instance.scanComplete();
                }

                return true;
            }

            return false;
        }
    }
}
