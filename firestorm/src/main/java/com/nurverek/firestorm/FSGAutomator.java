package com.nurverek.firestorm;

import com.nurverek.vanguard.VLDebug;
import com.nurverek.vanguard.VLList;
import com.nurverek.vanguard.VLListType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public final class FSGAutomator{

    private final FSG gen;

    protected VLListType<FSM> files;
    protected VLListType<Entry> entries;

    protected FSGAutomator(FSG gen, int filecapacity, int scancapacity){
        this.gen = gen;

        files = new VLListType<>(filecapacity, filecapacity);
        entries = new VLListType<>(scancapacity, scancapacity);
    }

    public void add(InputStream src, ByteOrder order, boolean fullsizedposition, int estimatedsize) throws IOException{
        FSM data = new FSM();
        data.loadFromFile(src, order, fullsizedposition, estimatedsize);

        files.add(data);
    }

    public FSMesh register(FSGBluePrint blueprint, String name){
        Entry e = new Entry(blueprint, name);
        e.register(gen);

        entries.add(e);

        return e.scanner.mesh;
    }

    public void run(int debug){
        int size = entries.size();

        Entry entry;
        FSGScanner scanner;
        FSGBluePrint blueprint;
        FSMesh mesh;

        build(debug);

        FSBufferManager buffermanager = gen.bufferManager();

        for(int i = 0; i < size; i++){
            scanner = entries.get(i).scanner;

            blueprint = entries.get(i).blueprint;
            blueprint.postScanAdjustment(scanner.mesh);
            blueprint.createLinks(scanner.mesh);

            scanner.layout = blueprint.bufferLayouts(scanner.mesh, buffermanager);
        }

        buffer(debug);

        for(int i = 0; i < size; i++){
            entry = entries.get(i);

            mesh = entry.scanner.mesh;

            blueprint = entry.blueprint;
            blueprint.postBufferAdjustment(mesh);
            blueprint.attachMeshToPrograms(mesh);
            blueprint.finished(mesh);
        }
    }

    private void build(int debug){
        int entrysize = entries.size();

        if(debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();

            FSGScanner s;
            boolean found;

            VLDebug.printDirect("[Assembler Check Stage]\n");

            for(int i = 0; i < entrysize; i++){
                s = entries.get(i).scanner;

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

            int filesize = files.size();

            FSMesh mesh;
            FSM.Data d;
            VLListType<FSM.Data> data;
            int datasize;

            for(int i = 0; i < filesize; i++){
                data = files.get(i).data;
                datasize = data.size();

                for(int i2 = 0; i2 < datasize; i2++){
                    d = data.get(i2);

                    for(int i3 = 0; i3 < entrysize; i3++){
                        s = entries.get(i3).scanner;
                        mesh = s.mesh;
                        found = false;

                        try{
                            if(s.scan(this, d) && debug >= FSControl.DEBUG_FULL){
                                VLDebug.append("Built[");
                                VLDebug.append(i2);
                                VLDebug.append("] keyword[");
                                VLDebug.append(s.name);
                                VLDebug.append("] name[");
                                VLDebug.append(d.name);
                                VLDebug.append("] ");

                                found = true;
                            }

                            if(found && mesh.size() > 1){
                                FSInstance instance1 = mesh.first();
                                FSInstance instance2 = mesh.instance(mesh.size() - 1);

                                if(instance1.positions().size() != instance2.positions().size()){
                                    VLDebug.printD();
                                    VLDebug.append("[WARNING] [Attempting to do instancing on meshes with different vertex characteristics] [Instance1_Vertex_Size[");
                                    VLDebug.append(instance1.positions().size());
                                    VLDebug.append("] Instance2_Vertex_Size[");
                                    VLDebug.append(instance2.positions().size());
                                    VLDebug.append("]");
                                    VLDebug.printE();
                                }
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
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printD();
            VLDebug.printDirect("[Checking Scan Results]\n");

            for(int i = 0; i < entrysize; i++){
                s = entries.get(i).scanner;

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
            int filesize = files.size();

            FSMesh mesh;
            FSM.Data d;
            VLListType<FSM.Data> data;
            int datasize;

            for(int i = 0; i < filesize; i++){
                data = files.get(i).data;
                datasize = data.size();

                for(int i2 = 0; i2 < datasize; i2++){
                    d = data.get(i2);

                    for(int i3 = 0; i3 < entrysize; i3++){
                        entries.get(i3).scanner.scan(this, d);
                    }
                }
            }
        }
    }

    private void buffer(int debug){
        int size = entries.size();

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
                s = entries.get(i).scanner;

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
                entries.get(i).scanner.buffer();
            }

            gen.bufferManager().upload();
        }
    }

    private static final class Entry{

        protected FSGBluePrint blueprint;
        protected FSGScanner scanner;
        protected String name;

        protected Entry(FSGBluePrint blueprint, String name){
            this.blueprint = blueprint;
            this.name = name;
        }

        protected void register(FSG gen){
            scanner = blueprint.register(gen, name);
        }
    }
}
