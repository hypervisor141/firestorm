package hypervisor.firestorm.sync;

import hypervisor.firestorm.engine.FSR;
import hypervisor.firestorm.engine.FSRTask;
import hypervisor.vanguard.sync.VLSyncMap;
import hypervisor.vanguard.sync.VLSyncType;

public abstract class FSSyncMap<SOURCE, TARGET> extends VLSyncMap<SOURCE, TARGET>{

    public Post<SOURCE> post;

    public FSSyncMap(TARGET target){
        super(target);
        post = new Post<>(this);
    }

    public FSSyncMap(FSSyncMap<SOURCE, TARGET> src, long flags){
        copy(src, flags);
    }

    protected FSSyncMap(){

    }

    @Override
    public void sync(SOURCE source){
        post.update(source);
        FSR.postTask(post);
    }

    public abstract void syncPosted(SOURCE source);

    @Override
    public void copy(VLSyncType<SOURCE> src, long flags){
        super.copy(src, flags);
        post = ((FSSyncMap<SOURCE, TARGET>)src).post;
    }

    public static class Post<SOURCE> implements FSRTask{

        public FSSyncMap<SOURCE, ?> map;
        public SOURCE source;

        public Post(FSSyncMap<SOURCE, ?> map){
            this.map = map;
        }

        public void update(SOURCE source){
            this.source = source;
        }

        @Override
        public void run(){
            map.syncPosted(source);
        }
    }
}
