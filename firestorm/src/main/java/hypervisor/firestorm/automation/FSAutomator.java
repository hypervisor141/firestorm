package hypervisor.firestorm.automation;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.io.FSM;
import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.utils.VLLog;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class FSAutomator{

    protected VLListType<FileTarget> files;
    protected VLListType<FSHScanner> scanners;
    protected VLLog log;

    public FSAutomator(int filecapacity, int scancapacity){
        files = new VLListType<>(filecapacity, filecapacity);
        scanners = new VLListType<>(scancapacity, scancapacity);
    }

    protected FSAutomator(){

    }

    public void register(FileTarget entry){
        files.add(entry);
    }

    public void add(FSHScanner scanner){
        scanners.add(scanner);
    }

    public FileTarget get(int index){
        return files.get(index);
    }

    public int size(){
        return files.size();
    }

    public void build(int debug){
        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            }, 50);

            log.setDebugTagsOffsetHere();
            log.printInfo("[Automated Build Initiated]");

            fileDebugLoop("Scan Stage", log, new LoopOperation<FileTarget>(){

                @Override
                public void run(FileTarget target, VLLog log) throws Exception{
                    target.scan(FSAutomator.this);
                }
            });
            fileDebugLoop("Results Check Stage", log, new LoopOperation<FileTarget>(){

                @Override
                public void run(FileTarget target, VLLog log){
                    target.checkResults(FSAutomator.this, log);
                }
            });
            scannerDebugLoop("Signal Scan Complete", log, new LoopOperation<FSHScanner>(){

                @Override
                public void run(FSHScanner target, VLLog log){
                    target.signalScanComplete();
                }
            });
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
            scannerDebugLoop("Signal Build Complete Stage", log, new LoopOperation<FSHScanner>(){

                @Override
                public void run(FSHScanner target, VLLog log){
                    target.signalBuildComplete();
                }
            });

            log.printInfo("[Automated Buffer Procedure Complete]");

        }else{
            int size = files.size();

            for(int i = 0; i < size; i++){
                try{
                    files.get(i).scan(this);

                }catch(IOException ex){
                    throw new RuntimeException("IO error when loading file.", ex);
                }
            }

            size = scanners.size();

            for(int i = 0; i < size; i++){
                scanners.get(i).signalScanComplete();
            }
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
                scanners.get(i).signalBuildComplete();
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

        public FileTarget(InputStream src, ByteOrder order, boolean fullsizedposition){
            this.src = src;
            this.order = order;
            this.fullsizedposition = fullsizedposition;
        }

        protected FileTarget(){

        }

        void scan(FSAutomator automator) throws IOException{
            FSM fsm = new FSM();
            fsm.loadFromFile(src, order, fullsizedposition, scancapacity);
            VLListType<FSM.Data> content = fsm.data;
            VLListType<FSHScanner> scanners = automator.scanners;

            int size = content.size();
            int size2 = scanners.size();

            for(int i = 0; i < size; i++){
                FSM.Data data = content.get(i);

                for(int i2 = 0; i2 < size2; i2++){
                    scanners.get(i2).scan(automator, data);
                }
            }
        }

        void checkResults(FSAutomator automator, VLLog log){
            VLListType<FSHScanner> scanners = automator.scanners;
            int size = scanners.size();

            for(int i = 0; i < size; i++){
                FSHScanner entry = scanners.get(i);

                if(entry.target.size() == 0){
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
    }
}
