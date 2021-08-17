package hypervisor.firestorm.automation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import hypervisor.firestorm.io.FSM;
import hypervisor.firestorm.mesh.FSTypeInstance;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.vanguard.list.arraybacked.VLListType;

public interface FSScanTarget{

    void scan(FSTypeMesh<FSTypeInstance> target);
    void release();

    class FSMGroupTarget implements FSScanTarget{

        private VLListType<FSMTarget> targets;

        public FSMGroupTarget(int capacity, int resizeoverhead){
            targets = new VLListType<>(capacity, resizeoverhead);
        }

        protected FSMGroupTarget(){

        }

        @Override
        public void scan(FSTypeMesh<FSTypeInstance> target){
            int size = targets.size();

            for(int i = 0; i < size; i++){
                targets.get(i).scan(target);
            }
        }

        @Override
        public void release(){
            int size = targets.size();

            for(int i = 0; i < size; i++){
                targets.get(i).release();
            }

            targets = null;
        }
    }

    class FSMTarget implements FSScanTarget{

        protected InputStream src;
        protected ByteOrder order;
        protected boolean fullsizedposition;
        protected VLListType<hypervisor.firestorm.io.FSM.Data> cache;

        public FSMTarget(InputStream src, ByteOrder order, boolean fullsizedposition){
            this.src = src;
            this.order = order;
            this.fullsizedposition = fullsizedposition;
        }

        @Override
        public void scan(FSTypeMesh<FSTypeInstance> target){
            if(cache == null){
                try{
                    cache = FSM.decode(src, order, fullsizedposition);

                }catch(Exception ex){
                    throw new RuntimeException(ex);
                }
            }

            FSScanFunction scanner = target.scanFunction();
            FSHAssembler assembler = target.assembler();

            int size = cache.size();

            for(int i = 0; i < size; i++){
                hypervisor.firestorm.io.FSM.Data data = cache.get(i);

                if(!data.locked){
                    if(scanner.scan(target, assembler, data)){
                        break;
                    }
                }
            }
        }

        @Override
        public void release(){
            try{
                src.close();

            }catch(IOException ex){
                //
            }

            src = null;
            cache = null;
        }
    }
}
