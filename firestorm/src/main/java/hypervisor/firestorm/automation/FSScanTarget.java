package hypervisor.firestorm.automation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import hypervisor.firestorm.io.FSM;
import hypervisor.firestorm.mesh.FSTypeInstance;
import hypervisor.firestorm.mesh.FSTypeMesh;
import hypervisor.vanguard.list.arraybacked.VLListType;

public interface FSScanTarget{

    void scan(FSTypeMesh<FSTypeInstance> target) throws Exception;
    void release();

    final class FSM implements FSScanTarget{

        protected InputStream src;
        protected ByteOrder order;
        protected boolean fullsizedposition;
        protected VLListType<hypervisor.firestorm.io.FSM.Data> cache;

        public FSM(InputStream src, ByteOrder order, boolean fullsizedposition){
            this.src = src;
            this.order = order;
            this.fullsizedposition = fullsizedposition;
        }

        protected FSM(){

        }

        @Override
        public void scan(FSTypeMesh<FSTypeInstance> target) throws Exception{
            if(cache == null){
                cache = hypervisor.firestorm.io.FSM.decode(src, order, fullsizedposition);
            }

            FSScanFunction scanner = target.scanFunction();
            FSHAssembler assembler = target.assembler();

            int size = cache.size();

            for(int i = 0; i < size; i++){
                hypervisor.firestorm.io.FSM.Data data = cache.get(i);

                if(scanner.scan(target, assembler, data)){
                    break;

                }else if(data.locked){
                    break;
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
