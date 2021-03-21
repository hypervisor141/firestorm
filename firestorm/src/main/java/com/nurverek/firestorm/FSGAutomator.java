package com.nurverek.firestorm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import vanguard.VLBuffer;
import vanguard.VLDebug;
import vanguard.VLListType;

public final class FSGAutomator{

    private final FSG<?> gen;

    protected VLListType<FSM> files;
    protected VLListType<FSGScanner> entries;

    public FSGAutomator(FSG<?> gen, int filecapacity, int scancapacity){
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
        FSGScanner entry = blueprint.createScanner(name);
        entries.add(entry);

        return entry.mesh;
    }

    public void run(int debug){
        int size = entries.size();

        build(debug);

        for(int i = 0; i < size; i++){
            entries.get(i).signalMeshBuilt();
        }

        buffer(debug);

        for(int i = 0; i < size; i++){
            entries.get(i).signalFinished();
        }
    }

    private void build(int debug){
        int entrysize = entries.size();

        if(debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();

            FSGScanner entry;
            boolean found;

            VLDebug.printDirect("[Assembler Check Stage]\n");

            for(int i = 0; i < entrysize; i++){
                entry = entries.get(i);

                if(entry.assembler.checkDebug()){
                    VLDebug.append("Scanner[");
                    VLDebug.append(entry.name);
                    VLDebug.append("] : invalid assembler configuration.");
                    VLDebug.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(VLDebug.get(), null);
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
                        entry = entries.get(i3);
                        mesh = entry.mesh;
                        found = false;

                        try{
                            if(entry.scan(this, d) && debug >= FSControl.DEBUG_FULL){
                                VLDebug.append("Built[");
                                VLDebug.append(i2);
                                VLDebug.append("] keyword[");
                                VLDebug.append(entry.name);
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
                            VLDebug.append(entry.name);
                            VLDebug.append("\"\n[Assembler Configuration]\n");

                            entry.assembler.stringify(VLDebug.get(), null);
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
                entry = entries.get(i);

                if(entry.mesh.size() == 0){
                    VLDebug.append("Scan incomplete : found no instance for mesh with keyword \"");
                    VLDebug.append(entry.name);
                    VLDebug.append("\".\n");
                    VLDebug.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(VLDebug.get(), null);

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
                        entries.get(i3).scan(this, d);
                    }
                }
            }
        }
    }

    private void buffer(int debug){
        int size = entries.size();

        if(debug > FSControl.DEBUG_DISABLED){
            VLDebug.recreate();
            FSGScanner entry;

            VLDebug.printDirect("[Buffering Stage]\n");

            for(int i = 0; i < size; i++){
                entry = entries.get(i);

                VLDebug.append("Buffering[");
                VLDebug.append(i + 1);
                VLDebug.append("/");
                VLDebug.append(size);
                VLDebug.append("]\n");

                if(debug >= FSControl.DEBUG_FULL){
                    entry.debugInfo();
                }

                try{
                    entry.bufferDebug();

                }catch(Exception ex){
                    VLDebug.append("Error buffering \"");
                    VLDebug.append(entry.name);
                    VLDebug.append("\"\n");
                    VLDebug.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(VLDebug.get(), null);
                    VLDebug.printE();

                    throw new RuntimeException(ex);
                }

                VLDebug.printD();
            }

            VLListType<FSVertexBuffer<VLBuffer<?, ?>>> buffers = gen.buffers();

            try{
                int buffersize = buffers.size();

                for(int i = 0; i < buffersize; i++){
                    buffers.get(i).upload();
                }

            }catch(Exception ex){
                VLDebug.printE();
                throw new RuntimeException("Failed to upload buffers", ex);
            }

            VLDebug.printDirect("[DONE]\n");
            VLDebug.printD();

        }else{
            VLListType<FSVertexBuffer<VLBuffer<?, ?>>> buffers = gen.buffers();
            int buffersize = buffers.size();

            for(int i = 0; i < buffersize; i++){
                buffers.get(i).initialize();
            }
            for(int i = 0; i < size; i++){
                entries.get(i).buffer();
            }
            for(int i = 0; i < buffersize; i++){
                buffers.get(i).upload();
            }
        }
    }
}
