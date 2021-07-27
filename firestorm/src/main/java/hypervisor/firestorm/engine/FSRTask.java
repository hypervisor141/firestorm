package hypervisor.firestorm.engine;

import hypervisor.vanguard.sync.VLSyncType;
import hypervisor.vanguard.utils.VLCopyable;

public interface FSRTask{

    void run();

    class SyncWrapper<SOURCE> implements FSRTask, VLCopyable<SyncWrapper<SOURCE>>{

        public VLSyncType<SOURCE> target;
        public SOURCE source;

        public SyncWrapper(VLSyncType<SOURCE> target){
            this.target = target;
        }

        public SyncWrapper(SyncWrapper<SOURCE> src, long flags){
            copy(src, flags);
        }

        protected SyncWrapper(){

        }

        public void update(SOURCE source){
            this.source = source;
        }

        @Override
        public void run(){
            target.sync(source);
        }

        @Override
        public void copy(SyncWrapper<SOURCE> src, long flags){
            source = src.source;

            if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
                this.target = src.target;

            }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
                this.target = src.target.duplicate(FLAG_DUPLICATE);

            }else{
                Helper.throwMissingAllFlags();
            }
        }

        @Override
        public SyncWrapper<SOURCE> duplicate(long flags){
            return new SyncWrapper<>(this, flags);
        }
    }
}
