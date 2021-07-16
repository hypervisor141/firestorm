package hypervisor.firestorm.automation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.io.FSM;
import hypervisor.vanguard.list.VLListType;
import hypervisor.vanguard.utils.VLLog;

public class FSAutomator{

    protected VLListType<FileTarget> files;
    protected VLListType<FSHScanner<?>> scanners;
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

    public void add(FSHScanner<?> scanner){
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
            scannerDebugLoop("Results Check Stage", log, new LoopOperation<FSHScanner<?>>(){

                @Override
                public void run(FSHScanner<?> target, VLLog log){
                    target.checkResults(log);
                }
            });
            scannerDebugLoop("Signal Scan Complete", log, new LoopOperation<FSHScanner<?>>(){

                @Override
                public void run(FSHScanner<?> target, VLLog log){
                    target.signalScanComplete();
                }
            });
            scannerDebugLoop("Measurement Stage", log, new LoopOperation<FSHScanner<?>>(){

                @Override
                public void run(FSHScanner<?> target, VLLog log){
                    target.accountForTargetSizeDebug(log);
                }
            });
            scannerDebugLoop("Buffer Build Stage", log, new LoopOperation<FSHScanner<?>>(){

                @Override
                public void run(FSHScanner<?> target, VLLog log){
                    target.bufferDebug(log);
                }
            });
            scannerDebugLoop("Buffer Upload Stage", log, new LoopOperation<FSHScanner<?>>(){

                @Override
                public void run(FSHScanner<?> target, VLLog log){
                    target.uploadBuffer();
                }
            });
            scannerDebugLoop("Signal Buffer Complete", log, new LoopOperation<FSHScanner<?>>(){

                @Override
                public void run(FSHScanner<?> target, VLLog log){
                    target.signalBufferComplete();
                }
            });
            scannerDebugLoop("Signal Build Complete Stage", log, new LoopOperation<FSHScanner<?>>(){

                @Override
                public void run(FSHScanner<?> target, VLLog log){
                    target.finalizeBuild();
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
                scanners.get(i).accountForTargetSize();
            }
            for(int i = 0; i < size; i++){
                scanners.get(i).buffer();
            }
            for(int i = 0; i < size; i++){
                scanners.get(i).uploadBuffer();
            }
            for(int i = 0; i < size; i++){
                scanners.get(i).finalizeBuild();
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

    private void scannerDebugLoop(String title, VLLog log, LoopOperation<FSHScanner<?>> task){
        log.addTag(title);

        int size = scanners.size();

        for(int i = 0; i < size; i++){
            FSHScanner<?> entry = scanners.get(i);
            log.addTag(entry.target.name());
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

        public FileTarget(InputStream src, ByteOrder order, boolean fullsizedposition){
            this.src = src;
            this.order = order;
            this.fullsizedposition = fullsizedposition;
        }

        protected FileTarget(){

        }

        void scan(FSAutomator automator) throws IOException{
            final VLListType<FSHScanner<?>> scanners = automator.scanners;
            final int size = scanners.size();

            FSM.loadFromFile(src, order, fullsizedposition, new FSM.DataOperator(){

                @Override
                public void operate(FSM.Data data){
                    for(int i = 0; i < size; i++){
                        scanners.get(i).scan(data);

                        if(data.locked){
                            return;
                        }
                    }
                }
            });
        }
    }
}
