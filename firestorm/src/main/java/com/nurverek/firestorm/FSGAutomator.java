package com.nurverek.firestorm;

import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLListType;

public final class FSGAutomator{

    private final FSG gen;

    protected FSM fsm;
    protected VLListType<Entry> entries;

    protected FSGAutomator(FSG gen, FSM fsm, int capacity){
        this.gen = gen;
        this.fsm = fsm;

        entries = new VLListType<>(capacity, capacity);
    }

    public void register(FSGBluePrint blueprint, String name){
        entries.add(new Entry(blueprint, name));
    }

    public void run(int debug){
        int size = entries.size();
        VLListType<FSGScanner> scanners = new VLListType<>(size, size);

        Entry entry;
        FSGScanner scanner;
        FSGBluePrint blueprint;
        FSMesh mesh;

        for(int i = 0; i < size; i++){
            scanners.add(entries.get(i).register(gen));
        }

        build(scanners, debug);

        FSBufferManager buffermanager = gen.bufferManager();

        for(int i = 0; i < size; i++){
            scanner = scanners.get(i);

            blueprint = entries.get(i).blueprint;
            blueprint.postScanAdjustment(scanner.mesh);
            blueprint.createLinks(scanner.mesh);

            scanner.layout = blueprint.bufferLayouts(scanner.mesh, buffermanager);
        }

        buffer(scanners, debug);

        for(int i = 0; i < size; i++){
            mesh = scanners.get(i).mesh;

            blueprint = entries.get(i).blueprint;
            blueprint.postBufferAdjustment(mesh);
            blueprint.attachMeshToPrograms(mesh);
            blueprint.finished(mesh);
        }
    }

    private void build(VLListType<FSGScanner> scanners, int debug){
        VLListType<FSM.Data> data = fsm.data;
        FSM.Data d;

        int size = data.size();
        int size2 = scanners.size();

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
        int size = scanners.size();

        if(debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();
            FSGScanner s;

            VLDebug.printDirect("[Buffering Stage]\n");

            try{
                gen.bufferManager().initialize();

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
                gen.bufferManager().upload();

            }catch(Exception ex){
                VLDebug.printE();
                throw new RuntimeException("Failed to upload buffers", ex);
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printD();

        }else{
            gen.bufferManager().initialize();

            for(int i = 0; i < size; i++){
                scanners.get(i).buffer();
            }

            gen.bufferManager().upload();
        }
    }

    private static final class Entry{

        protected FSGBluePrint blueprint;
        protected String name;

        protected Entry(FSGBluePrint blueprint, String name){
            this.blueprint = blueprint;
            this.name = name;
        }

        protected FSGScanner register(FSG gen){
            return blueprint.register(gen, name);
        }
    }
}
