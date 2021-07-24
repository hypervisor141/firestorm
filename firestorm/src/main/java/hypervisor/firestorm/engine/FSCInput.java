package hypervisor.firestorm.engine;

import android.view.MotionEvent;

import hypervisor.firestorm.mesh.FSTypeRender;
import hypervisor.vanguard.list.VLListType;

public final class FSCInput{

    public static final VLListType<Processor> TYPE_PRE_TOUCH = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_PRE_DOWN = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_PRE_SINGLETAP = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_PRE_LONGPRESS = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_PRE_SHOWPRESS = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_PRE_SCROLL = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_PRE_FLING = new VLListType<>(20, 50);

    public static final VLListType<Processor> TYPE_TOUCH = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_DOWN = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_SINGLETAP = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_LONGPRESS = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_SHOWPRESS = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_SCROLL = new VLListType<>(20, 50);
    public static final VLListType<Processor> TYPE_FLING = new VLListType<>(20, 50);

    public static final Object LOCK = new Object();

    public static void trigger(VLListType<Processor> pretype, VLListType<Processor> type, MotionEvent e1, MotionEvent e2, float f1, float f2){
        Processor[] cache;

        synchronized(LOCK){
            int size = pretype.size();
            int size2 = type.size();

            cache = new Processor[size + size2];

            Object[] array = pretype.array();
            Object[] array2 = type.array();

            for(int i = 0; i < size; i++){
                cache[i] = (Processor)array[i];
            }
            for(int i = 0, i2 = size; i < size2; i++, i2++){
                cache[i2] = (Processor)array2[i];
            }
        }

        float[] near = new float[4];
        float[] far = new float[4];

        int size = cache.length;

        FSR.requestSyncWindow();

        FSView config = FSControl.getView();
        config.unProject2DPoint(e1.getX(), e1.getY(), near, 0, far, 0);

        for(int i = 0; i < size; i++){
            cache[i].process(e1, e2, f1, f2, near, far);
        }

        FSR.notifySyncCompleted();
    }

    protected static void destroy(boolean destroyonpause){
        if(destroyonpause){
            TYPE_PRE_TOUCH.resize(0);
            TYPE_PRE_DOWN.resize(0);
            TYPE_PRE_SINGLETAP.resize(0);
            TYPE_PRE_LONGPRESS.resize(0);
            TYPE_PRE_SHOWPRESS.resize(0);
            TYPE_PRE_SCROLL.resize(0);
            TYPE_PRE_FLING.resize(0);

            TYPE_TOUCH.resize(0);
            TYPE_DOWN.resize(0);
            TYPE_SINGLETAP.resize(0);
            TYPE_LONGPRESS.resize(0);
            TYPE_SHOWPRESS.resize(0);
            TYPE_SCROLL.resize(0);
            TYPE_FLING.resize(0);
        }
    }

    public interface Processor{

        void process(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }

    public static class TypeRenderProcessor implements Processor{

        public FSTypeRender target;

        public TypeRenderProcessor(FSTypeRender target){
            this.target = target;
        }

        protected TypeRenderProcessor(){

        }

        @Override
        public void process(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
            target.checkInputs(e1, e2, f1, f2, near, far);
        }
    }
}
