package hypervisor.firestorm.sync;

import hypervisor.firestorm.engine.FSR;
import hypervisor.firestorm.engine.FSRTask;
import hypervisor.vanguard.sync.VLSyncMap;
import hypervisor.vanguard.sync.VLSyncType;
import hypervisor.vanguard.utils.VLCopyable;

public abstract class FSSyncMap<SOURCE, TARGET extends VLSyncType<SOURCE>> extends VLSyncMap<SOURCE, TARGET>{

    public Post<SOURCE> post;

    public FSSyncMap(TARGET target){
        super(target);
        post = new Post<>(target);
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

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            post = ((FSSyncMap<SOURCE, TARGET>)src).post;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            post = ((FSSyncMap<SOURCE, TARGET>)src).post;

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    public static class Post<SOURCE> implements FSRTask, VLCopyable<Post<SOURCE>>{

        public VLSyncType<SOURCE> target;
        public SOURCE source;

        public Post(VLSyncType<SOURCE> target){
            this.target = target;
        }

        public Post(Post<SOURCE> src, long flags){
            copy(src, flags);
        }

        protected Post(){

        }

        public void update(SOURCE source){
            this.source = source;
        }

        @Override
        public void run(){
            target.sync(source);
        }

        @Override
        public void copy(Post<SOURCE> src, long flags){
            Post<SOURCE> target = (Post<SOURCE>)src;
            source = target.source;

            if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
                this.target = target.target;

            }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
                this.target = target.target.duplicate(FLAG_DUPLICATE);

            }else{
                Helper.throwMissingAllFlags();
            }
        }

        @Override
        public Post<SOURCE> duplicate(long flags){
            return new Post<>(this, flags);
        }
    }
}
