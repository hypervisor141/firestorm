package com.nurverek.firestorm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import vanguard.VLListType;
import vanguard.VLLog;

public class FSAutomator{

    protected VLListType<FSM> files;
    protected VLListType<FSHScanner> entries;
    protected VLListType<FSVertexBuffer<?>> buffers;
    protected VLLog log;

    protected FSAutomator(int filecapacity, int scancapacity, int buffercapacity){
        files = new VLListType<>(filecapacity, filecapacity);
        entries = new VLListType<>(scancapacity, scancapacity);
        buffers = new VLListType<>(buffercapacity, buffercapacity);
    }

    public void addFile(InputStream src, ByteOrder order, boolean fullsizedposition, int estimatedsize) throws IOException{
        FSM data = new FSM();
        data.loadFromFile(src, order, fullsizedposition, estimatedsize);

        files.add(data);
    }

    public void addScanner(FSHScanner entry){
        entries.add(entry);
    }

    public void addBuffer(FSVertexBuffer<?> buffer){
        buffers.add(buffer);
    }

    public void scan(int debug){
        int entrysize = entries.size();

        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            });

            FSHScanner entry;
            boolean found;

            log.printInfo("[Assembler Check Stage]");

            for(int i = 0; i < entrysize; i++){
                entry = entries.get(i);
                entry.assembler.checkDebug();

                log.append("Scanner[");
                log.append(entry.name);
                log.append("] [Invalid Assembler Configuration]\n");
                log.append("[Assembler Configuration]\n");

                entry.assembler.stringify(log.get(), null);
            }

            log.printInfo();
            log.printInfo("[DONE]");
            log.printInfo("[Build Stage]");

            int filesize = files.size();

            for(int i = 0; i < filesize; i++){
                VLListType<FSM.Data> data = files.get(i).data;
                int datasize = data.size();

                for(int i2 = 0; i2 < datasize; i2++){
                    FSM.Data d = data.get(i2);

                    for(int i3 = 0; i3 < entrysize; i3++){
                        entry = entries.get(i3);
                        FSMesh mesh = entry.mesh;
                        found = false;

                        try{
                            if(entry.scan(this, d) && debug >= FSControl.DEBUG_FULL){
                                log.append("Built[");
                                log.append(i2);
                                log.append("] keyword[");
                                log.append(entry.name);
                                log.append("] name[");
                                log.append(d.name);
                                log.append("] ");

                                found = true;
                            }

                            if(found && mesh.size() > 1){
                                FSInstance instance1 = mesh.first();
                                FSInstance instance2 = mesh.get(mesh.size() - 1);

                                if(instance1.positions().size() != instance2.positions().size()){
                                    log.printInfo();
                                    log.append("[WARNING] [Attempting to do instancing on meshes with different vertex characteristics] [Instance1_Vertex_Size[");
                                    log.append(instance1.positions().size());
                                    log.append("] Instance2_Vertex_Size[");
                                    log.append(instance2.positions().size());
                                    log.append("]");
                                    log.printError();
                                }
                            }

                        }catch(Exception ex){
                            log.append("Error building \"");
                            log.append(entry.name);
                            log.append("\"\n[Assembler Configuration]\n");

                            entry.assembler.stringify(log.get(), null);
                            log.printError();

                            throw new RuntimeException(ex);
                        }

                        if(found){
                            log.printInfo();
                        }
                    }
                }
            }

            log.printInfo();
            log.printInfo("[DONE]");
            log.printInfo("[Checking Scan Results]");

            for(int i = 0; i < entrysize; i++){
                entry = entries.get(i);

                if(entry.mesh.size() == 0){
                    log.append("Scan incomplete : found no instance for mesh with keyword \"");
                    log.append(entry.name);
                    log.append("\".\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);

                    log.printError();
                    throw new RuntimeException("Mesh Scan Error");
                }

                entry.scanComplete();
            }

            log.printInfo();
            log.printInfo("[DONE]");

        }else{
            int filesize = files.size();

            for(int i = 0; i < filesize; i++){
                VLListType<FSM.Data> datalist = files.get(i).data;
                int datasize = datalist.size();

                for(int i2 = 0; i2 < datasize; i2++){
                    FSM.Data data = datalist.get(i2);

                    for(int i3 = 0; i3 < entrysize; i3++){
                        entries.get(i3).scan(this, data);
                    }
                }
            }

            for(int i = 0; i < entrysize; i++){
                entries.get(i).scanComplete();
            }
        }
    }

    public void buffer(int debug){
        int size = entries.size();

        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            });

            FSHScanner entry;

            int buffersize = buffers.size();

            log.printInfo("[Buffering Stage]");

            for(int i = 0; i < size; i++){
                entry = entries.get(i);

                log.append("accountingForMesh[");
                log.append(i + 1);
                log.append("/");
                log.append(size);
                log.append("]\n");

                try{
                    entry.accountForBufferSize();

                }catch(Exception ex){
                    log.append("Error accounting for buffer size \"");
                    log.append(entry.name);
                    log.append("\"\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.printInfo();
            }

            try{
                log.append("Initializing buffers...");
                FSVertexBuffer<?> buffer;

                for(int i = 0; i < buffersize; i++){
                    buffer = buffers.get(i);
                    buffer.provider().initialize(ByteOrder.nativeOrder());
                    buffer.initialize();
                }

                log.append("[SUCCESS]\n");

            }catch(Exception ex){
                log.append("[FAILED]");
                log.printError();

                throw new RuntimeException("Failed to initialize buffers", ex);
            }

            for(int i = 0; i < size; i++){
                entry = entries.get(i);

                log.append("Buffering[");
                log.append(i + 1);
                log.append("/");
                log.append(size);
                log.append("]\n");

                try{
                    entry.bufferDebugAndFinish(log);

                }catch(Exception ex){
                    log.append("Error buffering \"");
                    log.append(entry.name);
                    log.append("\"\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.printInfo();
            }

            try{
                log.append("Uploading buffers...");

                for(int i = 0; i < buffersize; i++){
                    buffers.get(i).upload();
                }

                log.append("[SUCCESS]\n");

            }catch(Exception ex){
                log.append("[FAILED]");
                log.printError();
                throw new RuntimeException("Failed to upload buffers", ex);
            }

            for(int i = 0; i < size; i++){
                entries.get(i).bufferComplete();
            }

            log.printInfo("[DONE]");

        }else{
            int buffersize = buffers.size();

            for(int i = 0; i < size; i++){
                entries.get(i).accountForBufferSize();
            }
            for(int i = 0; i < buffersize; i++){
                FSVertexBuffer<?>  buffer = buffers.get(i);
                buffer.provider().initialize(ByteOrder.nativeOrder());
                buffer.initialize();
            }
            for(int i = 0; i < size; i++){
                entries.get(i).bufferAndFinish();
            }
            for(int i = 0; i < buffersize; i++){
                buffers.get(i).upload();
            }
            for(int i = 0; i < size; i++){
                entries.get(i).bufferComplete();
            }
        }
    }
}
