package hypervisor.firestorm.automation;

import android.util.Log;

import java.io.InputStream;
import java.nio.ByteOrder;

import hypervisor.firestorm.engine.FSControl;
import hypervisor.firestorm.io.FSM;
import hypervisor.vanguard.list.arraybacked.VLListType;
import hypervisor.vanguard.utils.VLLog;

public class FSAutomator{

    protected VLListType<Target> targets;
    protected VLListType<FSHScanner<?>> scanners;
    protected VLLog log;

    public FSAutomator(int targetcapacity, int scancapacity){
        targets = new VLListType<>(targetcapacity, targetcapacity);
        scanners = new VLListType<>(scancapacity, scancapacity);
    }

    protected FSAutomator(){

    }

    public void register(Target target){
        targets.add(target);
    }

    public void clearTargets(){
        targets.clear();
    }

    public void add(FSHScanner<?> scanner){
        scanners.add(scanner);
    }

    public Target get(int index){
        return targets.get(index);
    }

    public int size(){
        return targets.size();
    }

    public void build(int debug){
        if(debug > FSControl.DEBUG_DISABLED){
            log = new VLLog(new String[]{
                    FSControl.LOGTAG, getClass().getSimpleName()
            }, 10);

            log.setDebugTagsOffsetHere();
            log.printInfo("[Automated Build Initiated]");

            targetDebugLoop("Scan Stage", log, (target, log) -> target.scan(FSAutomator.this));
            scannerDebugLoop("Results Check Stage", log, FSHScanner::checkResults);
            scannerDebugLoop("Signal Scan Complete", log, (target, log) -> target.signalScanComplete());
            scannerDebugLoop("Measurement Stage", log, FSHScanner::accountForTargetSizeDebug);
            scannerDebugLoop("Buffer Build Stage", log, FSHScanner::bufferDebug);
            scannerDebugLoop("Buffer Upload Stage", log, (target, log) -> target.uploadBuffer());
            scannerDebugLoop("Signal Buffer Complete", log, (target, log) -> target.signalBufferComplete());
            scannerDebugLoop("Signal Build Complete Stage", log, (target, log) -> target.finalizeBuild());

            log.printInfo("[Automated Buffer Procedure Complete]");
            log = null;

        }else{
            int size = targets.size();

            for(int i = 0; i < size; i++){
                try{
                    targets.get(i).scan(this);

                }catch(Exception ex){
                    throw new RuntimeException("Error during target scan.", ex);
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

        scanners.clear();

        int size = targets.size();

        for(int i = 0; i < size; i++){
            if(!targets.get(i).keepInCache()){
                targets.remove(i);
                i--;
            }
        }
    }

    private void targetDebugLoop(String title, VLLog log, LoopOperation<Target> task){
        log.addTag(title);

        int size = targets.size();

        for(int i = 0; i < size; i++){
            Target entry = targets.get(i);
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

    public interface Target{

        void scan(FSAutomator automator) throws Exception;
        boolean keepInCache();
    }

    public final static class FSMTarget implements Target{

        protected InputStream src;
        protected ByteOrder order;
        protected boolean fullsizedposition;
        protected boolean enablecache;
        protected VLListType<FSM.Data> cache;

        public FSMTarget(InputStream src, ByteOrder order, boolean fullsizedposition, boolean enablecache){
            this.src = src;
            this.order = order;
            this.fullsizedposition = fullsizedposition;
            this.enablecache = enablecache;
        }

        protected FSMTarget(){

        }

        @Override
        public void scan(FSAutomator automator) throws Exception{
            final VLListType<FSHScanner<?>> scanners = automator.scanners;
            final int size = scanners.size();

            if(cache == null){
                cache = FSM.decode(src, order, fullsizedposition, data -> {
                    for(int i = 0; i < size; i++){
                        scanners.get(i).scan(data);

                        if(data.locked){
                            return;
                        }
                    }

                }, enablecache);

            }else{
                int size2 = cache.size();

                for(int i = 0; i < size2; i++){
                    FSM.Data data = cache.get(i);

                    for(int i2 = 0; i2 < size; i2++){
                        scanners.get(i2).scan(data);

                        if(data.locked){
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public boolean keepInCache(){
            return enablecache;
        }
    }
}
