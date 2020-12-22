package com.nurverek.firestorm;

import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListType;

public final class FSGAutomator{

    private final FSG gen;

    protected FSM fsm;
    protected VLListType<FSG.BluePrint> blueprints;

    protected FSGAutomator(FSG gen, FSM fsm){
        this.gen = gen;
        this.fsm = fsm;

        int size = fsm.data.size();
        blueprints = new VLListType<>(size, size);
    }

    public void register(FSG.BluePrint blueprint){
        blueprints.add(blueprint);
    }

    public void run(int debug){
        int size = blueprints.size();
        VLListType<FSGScanner> scanners = new VLListType<>(size, 0);

        for(int i = 0; i < size; i++){
            scanners.add(blueprints.get(i).register(gen));
        }

        build(scanners, debug);

        FSGScanner s;
        FSG.BluePrint bp;

        for(int i = 0; i < size; i++){
            s = scanners.get(i);
            bp = blueprints.get(i);

            bp.buffer(s.mesh, s.layout);
            bp.makeLinks(s.mesh);
        }

        buffer(scanners, debug);

        for(int i = 0; i < size; i++){
            blueprints.get(i).program(scanners.get(i).mesh);
        }

        program(scanners, debug);
    }

    private void build(VLListType<FSGScanner> scanners, int debug){
        VLListType<FSM.Data> data = fsm.data;
        FSM.Data d;

        int size = data.size();
        int size2 = blueprints.size();

        if(debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();

            FSGScanner s;
            boolean found;

            VLDebug.printDirect("[Assembler Check Stage]\n");

            for(int i = 0; i < size2; i++){
                s = scanners.get(i);

                if(s.assembler.checkDebug()){
                    VLDebug.append("Scanner[");
                    VLDebug.append(s.name);
                    VLDebug.append("] : invalid assembler configuration.");
                    VLDebug.append("[Assembler Configuration]\n");

                    s.assembler.stringify(VLDebug.get(), null);
                }
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printDirect("[Build Stage]\n");

            FSMesh mesh;

            for(int i = 0; i < size; i++){
                d = data.get(i);

                for(int i2 = 0; i2 < size2; i2++){
                    s = scanners.get(i2);
                    mesh = s.mesh;
                    found = false;

                    try{
                        if(s.scan(this, d) && debug >= FSControl.DEBUG_FULL){
                            VLDebug.append("Built[");
                            VLDebug.append(i);
                            VLDebug.append("] keyword[");
                            VLDebug.append(s.name);
                            VLDebug.append("] name[");
                            VLDebug.append(d.name);
                            VLDebug.append("] ");

                            found = true;
                        }

                        if(found && mesh.size() > 1 && mesh.instance(mesh.size() - 1).positions().size() != mesh.first().positions().size()){
                            VLDebug.printD();
                            VLDebug.append("[WARNING] ");
                            VLDebug.append("[Attempting to do instancing on meshes with different vertex characteristics]");
                            VLDebug.printE();
                        }

                    }catch(Exception ex){
                        VLDebug.append("Error building \"");
                        VLDebug.append(s.name);
                        VLDebug.append("\"\n[Assembler Configuration]\n");

                        s.assembler.stringify(VLDebug.get(), null);
                        VLDebug.printE();

                        throw new RuntimeException(ex);
                    }

                    if(found){
                        VLDebug.printD();
                    }
                }
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printD();
            VLDebug.printDirect("[Checking Scan Results]\n");

            for(int i = 0; i < size2; i++){
                s = scanners.get(i);

                if(s.mesh.size() == 0){
                    VLDebug.append("Scan incomplete : found no instance for mesh with keyword \"");
                    VLDebug.append(s.name);
                    VLDebug.append("\".\n");
                    VLDebug.append("[Assembler Configuration]\n");

                    s.assembler.stringify(VLDebug.get(), null);

                    VLDebug.printE();
                    throw new RuntimeException("Mesh Scan Error");
                }
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printD();

        }else{
            for(int i = 0; i < size; i++){
                d = data.get(i);

                for(int i2 = 0; i2 < size2; i2++){
                    scanners.get(i2).scan(this, d);
                }
            }
        }
    }

    private void buffer(VLListType<FSGScanner> scanners, int debug){
        int size = blueprints.size();

        if(debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();
            FSGScanner s;

            VLDebug.printDirect("[Buffering Stage]\n");

            try{
                gen.BUFFERMANAGER.initialize();

            }catch(Exception ex){
                VLDebug.printE();
                throw new RuntimeException("Failed to initialize buffers", ex);
            }

            for(int i = 0; i < size; i++){
                s = scanners.get(i);

                VLDebug.append("Buffering[");
                VLDebug.append(i + 1);
                VLDebug.append("/");
                VLDebug.append(size);
                VLDebug.append("]\n");

                if(debug >= FSControl.DEBUG_FULL){
                    s.debugInfo();
                }

                try{
                    s.bufferDebug();

                }catch(Exception ex){
                    VLDebug.append("Error buffering \"");
                    VLDebug.append(s.name);
                    VLDebug.append("\"\n");
                    VLDebug.append("[Assembler Configuration]\n");

                    s.assembler.stringify(VLDebug.get(), null);
                    VLDebug.printE();

                    throw new RuntimeException(ex);
                }

                VLDebug.printD();
            }

            try{
                gen.BUFFERMANAGER.upload();

            }catch(Exception ex){
                VLDebug.printE();
                throw new RuntimeException("Failed to upload buffers", ex);
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printD();

        }else{
            gen.BUFFERMANAGER.initialize();

            for(int i = 0; i < size; i++){
                scanners.get(i).buffer();
            }

            gen.BUFFERMANAGER.upload();
        }
    }

    private void program(VLListType<FSGScanner> scanners, int debug){
        int size = blueprints.size();

        if(debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();
            FSGScanner s;

            VLDebug.printDirect("[Program Stage]\n");

            for(int i = 0; i < size; i++){
                s = scanners.get(i);

                VLDebug.append("Adding Mesh To Programs [");
                VLDebug.append(i + 1);
                VLDebug.append("/");
                VLDebug.append(size);
                VLDebug.append("]\n");

                try{
                    s.populatePrograms();

                }catch(Exception ex){
                    VLDebug.append("Error adding mesh to programs\"");
                    VLDebug.append(s.name);
                    VLDebug.append("\"\n");
                    VLDebug.append("[Assembler Configuration]\n");

                    s.assembler.stringify(VLDebug.get(), null);
                    VLDebug.printE();

                    throw new RuntimeException(ex);
                }

                VLDebug.printD();
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printD();

        }else{
            for(int i = 0; i < size; i++){
                scanners.get(i).populatePrograms();
            }
        }
    }
}
