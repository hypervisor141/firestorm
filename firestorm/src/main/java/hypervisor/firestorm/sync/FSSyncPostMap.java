package hypervisor.firestorm.sync;

import hypervisor.firestorm.engine.FSR;
import hypervisor.firestorm.engine.FSRTask;
import hypervisor.vanguard.sync.VLSyncMap;
import hypervisor.vanguard.sync.VLSyncType;
import hypervisor.vanguard.utils.VLCopyable;

public class FSSyncPostMap<SOURCE, TARGET extends VLSyncType<SOURCE>> extends VLSyncMap<SOURCE, TARGET>{

    public Post<SOURCE> post;

    public FSSyncPostMap(TARGET target){
        super(target);
        post = new Post<>(target);
    }

    public FSSyncPostMap(FSSyncPostMap<SOURCE, TARGET> src, long flags){
        copy(src, flags);
    }

    protected FSSyncPostMap(){

    }

    @Override
    public void sync(SOURCE source){
        post.update(source, target);
        FSR.postTask(post);
    }

    @Override
    public void copy(VLSyncType<SOURCE> src, long flags){
        super.copy(src, flags);

        if((flags & FLAG_REFERENCE) == FLAG_REFERENCE){
            post = ((FSSyncPostMap<SOURCE, TARGET>)src).post;

        }else if((flags & FLAG_DUPLICATE) == FLAG_DUPLICATE){
            post = ((FSSyncPostMap<SOURCE, TARGET>)src).post.duplicate(VLCopyable.FLAG_DUPLICATE);

        }else{
            Helper.throwMissingAllFlags();
        }
    }

    @Override
    public FSSyncPostMap<SOURCE, TARGET> duplicate(long flags){
        return new FSSyncPostMap<>(this, flags);
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

        public void update(SOURCE source, VLSyncType<SOURCE> target){
            this.source = source;
            this.target = target;
        }

        @Override
        public void run(){
            target.sync(source);
        }

        @Override
        public void copy(Post<SOURCE> src, long flags){
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
        public Post<SOURCE> duplicate(long flags){
            return new Post<>(this, flags);
        }
    }
}
