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

    public FileTarget get(int index){
        return files.get(index);
    }

    public int size(){
        return files.size();
    }

    public void scan(int debug){
        scanners = new VLListType<>(100, 100);

        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            }, 50);

            log.setDebugTagsOffsetHere();
            log.printInfo("[Automated Scan Initiated]");

            fileDebugLoop("Assembler Check Stage", log, new LoopOperation<FileTarget>(){

                @Override
                public void run(FileTarget target, VLLog log){
                    target.checkAssembler(log);
                }
            });
            fileDebugLoop("Build Stage", log, new LoopOperation<FileTarget>(){

                @Override
                public void run(FileTarget target, VLLog log) throws Exception{
                    target.scan(FSAutomator.this);
                }
            });
            fileDebugLoop("Results Check Stage", log, new LoopOperation<FileTarget>(){

                @Override
                public void run(FileTarget target, VLLog log){
                    target.checkResults(log);
                    target.offloadResults(FSAutomator.this);
                }
            });

            log.printInfo("[Automated Scan And Build Complete]");

        }else{
            int size = files.size();

            for(int i = 0; i < size; i++){
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
        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            }, 50);

            log.setDebugTagsOffsetHere();
            log.printInfo("[Automated Buffering Initiated]");

            scannerDebugLoop("Measurement Stage", log, new LoopOperation<FSHScanner>(){

                @Override
                public void run(FSHScanner target, VLLog log){
                    target.adjustBufferCapacityDebug(log);
                }
            });
            scannerDebugLoop("Buffer Build Stage", log, new LoopOperation<FSHScanner>(){

                @Override
                public void run(FSHScanner target, VLLog log){
                    target.bufferDebugAndFinish(log);
                }
            });
            scannerDebugLoop("Buffer Upload Stage", log, new LoopOperation<FSHScanner>(){

                @Override
                public void run(FSHScanner target, VLLog log){
                    target.uploadBuffer();
                }
            });
            scannerDebugLoop("Signal Complete Stage", log, new LoopOperation<FSHScanner>(){

                @Override
                public void run(FSHScanner target, VLLog log){
                    target.signalBufferComplete();
                }
            });

            log.printInfo("[Automated Buffer Procedure Complete]");

        }else{
            int size = scanners.size();

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

    private void fileDebugLoop(String title, VLLog log, LoopOperation<FileTarget> task){
        log.addTag(title);

        int size = files.size();

        for(int i = 0; i < size; i++){
            FileTarget entry = files.get(i);
            log.addTag(String.valueOf(i));

            try{
                task.run(entry, log);

                log.append("[SUCCESS]\n");
                log.printInfo();

            }catch(Exception ex){
                log.append("[FAILED]\n");
                log.printError();

                throw new RuntimeException(ex);
            }

            log.removeLastTag();
        }

        log.removeLastTag();
    }

    private void scannerDebugLoop(String title, VLLog log, LoopOperation<FSHScanner> task){
        log.addTag(title);

        int size = scanners.size();

        for(int i = 0; i < size; i++){
            FSHScanner entry = scanners.get(i);
            log.addTag(entry.name);
            log.addTag(String.valueOf(i));

            try{
                task.run(entry, log);

                log.append("[SUCCESS]\n");
                log.printInfo();

            }catch(Exception ex){
                log.append("[FAILED]\n");
                log.printError();

                throw new RuntimeException(ex);
            }

            log.removeLastTag();
            log.removeLastTag();
        }

        log.removeLastTag();
    }

    private interface LoopOperation<TARGET>{

        void run(TARGET target, VLLog log) throws Exception;
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

        public void register(Registrable target){
            scanners.add(target.generateScanner());
        }

        void checkAssembler(VLLog log){
            int size = scanners.size();

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);
                log.addTag(entry.name);

                try{
                    entry.assembler.checkDebug();
                    log.append("[SUCCESS]\n");
                    log.printInfo();

                }catch(Exception ex){
                    log.append("[FAILED]\n");
                    log.printError();

                    log.append("[Assembler Configuration]\n");

                    entry.assembler.log(log, null);
                    log.printError();

                    throw new RuntimeException("Invalid assembler configuration", ex);
                }

                log.removeLastTag();
            }
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

    public interface Registrable{

        FSHScanner generateScanner();
    }
}
