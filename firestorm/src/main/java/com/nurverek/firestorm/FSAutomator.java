package com.nurverek.firestorm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import vanguard.VLListType;
import vanguard.VLLog;

public class FSAutomator{

    protected VLListType<FileTarget> files;
    protected VLListType<FSHScanner> scanners;
    protected VLLog log;

    public FSAutomator(int filecapacity){
        files = new VLListType<>(filecapacity, filecapacity);
    }

    public FileTarget registerFile(InputStream src, ByteOrder order, boolean fullsizedposition, int estimatedsize){
        FileTarget entry = new FileTarget(src, order, fullsizedposition, estimatedsize);
        files.add(entry);

        return entry;
    }

    public void scan(int debug){
        scanners = new VLListType<>(100, 100);
        int filesize = files.size();

        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            });

            log.printInfo("[Automated Scan Initiated]");
            log.printInfo("[Assembler Check Stage]");

            for(int i = 0; i < filesize; i++){
                FileTarget file = files.get(i);

                log.append("[");
                log.append(i + 1);
                log.append("/");
                log.append(filesize);
                log.append("]\n");

                try{
                    file.checkAssembler(log);

                }catch(Exception ex){
                    throw new RuntimeException(ex);
                }
            }

            log.printInfo("[Build Stage]");

            for(int i = 0; i < filesize; i++){
                FileTarget file = files.get(i);

                log.append("File[");
                log.append(i + 1);
                log.append("/");
                log.append(filesize);
                log.append("]\n");

                try{
                    file.scan(this);
                    log.append(" [SUCCESS]\n");

                }catch(IOException ex){
                    log.append(" [FAILED]\n");
                    throw new RuntimeException("Error loading from file", ex);
                }
            }

            log.printInfo();
            log.printInfo("[DONE]");
            log.printInfo("[Checking Scan Results]");

            for(int i = 0; i < filesize; i++){
                FileTarget file = files.get(i);

                log.append("File[");
                log.append(i + 1);
                log.append("/");
                log.append(filesize);
                log.append("]\n");

                try{
                    file.checkResults(log);
                    file.offloadResults(this);

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            log.printInfo();
            log.printInfo("[Automated Scan And Build Complete]");

        }else{
            for(int i = 0; i < filesize; i++){
                try{
                    FileTarget file = files.get(i);

                    file.scan(this);
                    file.offloadResults(this);

                }catch(IOException ex){
                    throw new RuntimeException("IO error when loading file.", ex);
                }
            }
        }

        int size = scanners.size();

        for(int i = 0; i < size; i++){
            scanners.get(i).signalScanComplete();
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

                    entry.assembler.log(log, null);
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
                    log.append("[Buffering Failed] [");
                    log.append(entry.name);
                    log.append("]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.log(log, null);
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

                    entry.assembler.log(log, null);
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

                    entry.assembler.log(log, null);
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

    public final static class FileTarget{

        protected InputStream src;
        protected ByteOrder order;
        protected boolean fullsizedposition;
        protected int scancapacity;
        protected VLListType<FSHScanner> scanners;

        private FileTarget(InputStream src, ByteOrder order, boolean fullsizedposition, int scancapacity){
            this.src = src;
            this.order = order;
            this.fullsizedposition = fullsizedposition;
            this.scancapacity = scancapacity;

            scanners = new VLListType<>(scancapacity, scancapacity);
        }

        public void register(FSHScanner scanner){
            scanners.add(scanner);
        }

        void checkAssembler(VLLog log){
            int size = scanners.size();

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);

                try{
                    log.append("Scanner[");
                    log.append(entry.name);
                    log.append("]");

                    entry.assembler.checkDebug();

                    log.append(" [SUCCESS]\n");

                }catch(Exception ex){
                    log.append("[FAILED] [Invalid assembler configuration detected]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.log(log, null);
                    log.printError();

                    throw new RuntimeException("Invalid assembler configuration", ex);
                }
            }

            log.printInfo();
            log.printInfo("[DONE]");
        }

        void scan(FSAutomator automator) throws IOException{
            FSM fsm = new FSM();
            fsm.loadFromFile(src, order, fullsizedposition, scancapacity);
            VLListType<FSM.Data> content = fsm.data;

            int size = content.size();
            int size2 = scanners.size();

            for(int i = 0; i < size; i++){
                FSM.Data data = content.get(i);

                for(int i2 = 0; i2 < size2; i2++){
                    scanners.get(i2).scan(automator, data);
                }
            }
        }

        void checkResults(VLLog log){
            int size = scanners.size();

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);

                if(entry.mesh.size() == 0){
                    log.append("Incomplete scan : found no instance for mesh with keyword[");
                    log.append(entry.name);
                    log.append("]\n");
                    log.append("[Assembler Configuration]\n");

                    entry.assembler.log(log, null);

                    log.printError();
                    throw new RuntimeException("Mesh Scan Error");
                }
            }
        }

        void offloadResults(FSAutomator automator){
            automator.scanners.add(scanners);
            scanners = null;
        }
    }
}
