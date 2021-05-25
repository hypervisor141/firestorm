package com.firestorm.automation;

import com.firestorm.io.FSM;
import com.firestorm.mesh.FSInstance;
import com.firestorm.mesh.FSMesh;
import com.firestorm.program.FSP;

import vanguard.utils.VLLog;

public abstract class FSHScanner{

    protected FSHAssembler assembler;
    protected FSBufferTargets buffertarget;
    protected FSP program;
    protected FSMesh<FSInstance> target;
    protected String name;

    protected FSHScanner(FSMesh<FSInstance> target, FSP program, FSBufferTargets buffetarget, FSHAssembler assembler, String name){
        this.target = target;
        this.program = program;
        this.buffertarget = buffetarget;
        this.assembler = assembler;
        this.name = name.toLowerCase();

        target.name(name);
    }

    protected FSHScanner(){

    }

    abstract boolean scan(FSAutomator automator, FSM.Data data);

    void signalScanComplete(){
        target.parentRoot().scanComplete();
    }

    void signalBuildComplete(){
        target.parentRoot().buildComplete();
    }

    void adjustBufferCapacity(){
        if(buffertarget != null){
            buffertarget.prepare(target);
        }
    }

    void adjustBufferCapacityDebug(VLLog log){
        if(buffertarget != null){
            buffertarget.prepareDebug(target, log);
        }
    }

    void bufferAndFinish(){
        if(buffertarget != null){
            buffertarget.buffer(target);
            program.meshes().add(target);
        }
    }

    void bufferDebugAndFinish(VLLog log){
        if(buffertarget != null){
            buffertarget.bufferDebug(target, log);
            program.meshes().add(target);

        }else{
            log.append("[Buffering disabled] ");
        }
    }

    void uploadBuffer(){
        if(buffertarget != null){
            buffertarget.upload();
        }
    }

    public static class Singular extends FSHScanner{

        public Singular(FSMesh<FSInstance> target, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String name){
            super(target, program, buffertarget, assembler, name);
        }

        protected Singular(){

        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.equalsIgnoreCase(name)){
                if(target.size() > 0){
                    throw new RuntimeException("Found more than one instance for a singular scanner [" + target.name() + "]");
                }

                FSInstance instance = new FSInstance(data.name);
                target.add(instance);

                assembler.buildFirst(instance, this, data);
                return true;
            }

            return false;
        }
    }

    public static class Instanced extends FSHScanner{

        public Instanced(FSMesh<FSInstance> target, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String substringname){
            super(target, program, buffertarget, assembler, substringname);
        }

        protected Instanced(){

        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.contains(name)){
                FSInstance instance = new FSInstance(data.name);
                target.add(instance);

                if(target.size() == 1){
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

        protected int copycount;

        public InstancedCopy(FSMesh<FSInstance> target, FSP program, FSBufferTargets buffertarget, FSHAssembler assembler, String prefixname, int copycount){
            super(target, program, buffertarget, assembler, prefixname);
            this.copycount = copycount;
        }

        protected InstancedCopy(){

        }

        @Override
        boolean scan(FSAutomator automator, FSM.Data data){
            if(data.name.contains(name)){
                FSInstance instance = new FSInstance(data.name);
                target.add(instance);

                assembler.buildFirst(instance, this, data);

                for(int i = 0; i < copycount; i++){
                    instance = new FSInstance(data.name);
                    target.add(instance);

                    assembler.buildFirst(instance, this, data);
                }

                return true;
            }

            return false;
        }
    }
}
