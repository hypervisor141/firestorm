package com.nurverek.firestorm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import vanguard.VLListType;
import vanguard.VLLog;

public class FSAutomator{

    protected VLListType<FileEntry> files;
    protected VLListType<FSHScanner> scanners;
    protected VLLog log;

    public FSAutomator(int filecapacity, int scancapacity){
        files = new VLListType<>(filecapacity, filecapacity);
        scanners = new VLListType<>(scancapacity, scancapacity);
    }

    public void registerFile(InputStream src, ByteOrder order, boolean fullsizedposition, int estimatedsize){
        files.add(new FileEntry(src, order, fullsizedposition, estimatedsize));
    }

    public void registerScanner(FSHScanner scanner){
        scanners.add(scanner);
    }

    public void scan(int debug){
        int entrysize = scanners.size();

        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            });

            boolean found;

            log.printInfo("[Automated Scan Initiated]");
            log.printInfo("[Assembler Check Stage]");

            for(int i = 0; i < entrysize; i++){
                FSHScanner entry = scanners.get(i);

                try{
                    log.append("Scanner[");
                    log.append(entry.name);
                    log.append("]\n");

                    entry.assembler.checkDebug();

                    log.append("[SUCCESS]");

                }catch(Exception ex){
                    log.append("[FAILED] [Invalid assembler configuration detected]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                    log.printError();

                    throw new RuntimeException("Invalid assembler configuration", ex);
                }
            }

            log.printInfo();
            log.printInfo("[DONE]");
            log.printInfo("[Build Stage]");

            int filesize = files.size();

            for(int i = 0; i < filesize; i++){
                FileEntry file = files.get(i);
                VLListType<FSM.Data> data = null;

                log.append("LoadingFile[");
                log.append(i + 1);
                log.append("/");
                log.append(filesize);
                log.append("]");

                try{
                    data = file.load();
                    log.append(" [SUCCESS]");

                }catch(IOException ex){
                    log.append(" [FAILED]");
                    throw new RuntimeException("Error loading from file", ex);
                }

                int datasize = data.size();

                for(int i2 = 0; i2 < datasize; i2++){
                    FSM.Data fsmdata = data.get(i2);

                    for(int i3 = 0; i3 < entrysize; i3++){
                        FSHScanner entry = scanners.get(i3);
                        FSMesh mesh = entry.mesh;
                        found = false;

                        try{
                            if(entry.scan(this, fsmdata) && debug >= FSControl.DEBUG_FULL){
                                log.append("Built[");
                                log.append(i2);
                                log.append("] keyword[");
                                log.append(entry.name);
                                log.append("] name[");
                                log.append(fsmdata.name);
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
                FSHScanner entry = scanners.get(i);

                if(entry.mesh.size() == 0){
                    log.append("Scan incomplete : found no instance for mesh with keyword \"");
                    log.append(entry.name);
                    log.append("\".\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);

                    log.printError();
                    throw new RuntimeException("Mesh Scan Error");
                }

                entry.signalScanComplete();
            }

            log.printInfo();
            log.printInfo("[Automated Scan And Build Complete]");

        }else{
            int filesize = files.size();

            for(int i = 0; i < filesize; i++){
                VLListType<FSM.Data> datalist = null;

                try{
                    files.get(i).load();

                }catch(IOException ex){
                    throw new RuntimeException("Failed to load file", ex);
                }

                int datasize = datalist.size();

                for(int i2 = 0; i2 < datasize; i2++){
                    FSM.Data data = datalist.get(i2);

                    for(int i3 = 0; i3 < entrysize; i3++){
                        scanners.get(i3).scan(this, data);
                    }
                }
            }

            for(int i = 0; i < entrysize; i++){
                scanners.get(i).signalScanComplete();
            }
        }
    }

    public void buffer(int debug){
        int size = scanners.size();

        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            });

            log.printInfo("[Automated Buffering Initiated]");
            log.printInfo("[Measuring Buffer Size]");

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);

                log.append("[measuringBufferSize] [");
                log.append(i + 1);
                log.append("/");
                log.append(size);
                log.append("]\n");

                try{
                    entry.adjustBufferCapacity();

                }catch(Exception ex){
                    log.append("[Error accounting for buffer size] [");
                    log.append(entry.name);
                    log.append("]\n [Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.printInfo();
            }

            log.printInfo("[Buffer Size Measurement Complete]");
            log.printInfo("[Buffering Data]");

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);

                log.append("[Buffering] [");
                log.append(i + 1);
                log.append("/");
                log.append(size);
                log.append("]\n");

                try{
                    entry.bufferDebugAndFinish(log);

                }catch(Exception ex){
                    log.append("[Failed to buffer] [");
                    log.append(entry.name);
                    log.append("]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.printInfo();
            }

            log.printInfo("[Buffer Complete]");
            log.printInfo("[Uploading Buffers]");

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);

                log.append("[Uploading] [");
                log.append(i + 1);
                log.append("/");
                log.append(size);
                log.append("]\n");

                try{
                    entry.uploadBuffer();

                }catch(Exception ex){
                    log.append("[Failed to upload] [");
                    log.append(entry.name);
                    log.append("]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.printInfo();
            }

            log.printInfo("[Upload Complete]");
            log.printInfo("[Signaling Buffer Complete]");

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);

                log.append("[Signalling] [");
                log.append(i + 1);
                log.append("/");
                log.append(size);
                log.append("]\n");

                try{
                    entry.signalBufferComplete();

                }catch(Exception ex){
                    log.append("[Failed to signal buffer completion] [");
                    log.append(entry.name);
                    log.append("]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.stringify(log.get(), null);
                    log.printError();

                    throw new RuntimeException(ex);
                }

                log.printInfo();
            }

            log.printInfo();
            log.printInfo("[Automated Buffer Procedure Complete]");

        }else{
            for(int i = 0; i < size; i++){
                scanners.get(i).adjustBufferCapacity();
            }
            for(int i = 0; i < size; i++){
                scanners.get(i).bufferAndFinish();
            }
            for(int i = 0; i < size; i++){
                scanners.get(i).uploadBuffer();
            }
            for(int i = 0; i < size; i++){
                scanners.get(i).signalBufferComplete();
            }
        }
    }

    private static final class FileEntry{

        public InputStream src;
        public ByteOrder order;
        public boolean fullsizedposition;
        public int estimatedsize;

        private FileEntry(InputStream src, ByteOrder order, boolean fullsizedposition, int estimatedsize){
            this.src = src;
            this.order = order;
            this.fullsizedposition = fullsizedposition;
            this.estimatedsize = estimatedsize;
        }

        private VLListType<FSM.Data> load() throws IOException{
            FSM fsm = new FSM();
            fsm.loadFromFile(src, order, fullsizedposition, estimatedsize);

            return fsm.data;
        }
    }
}
