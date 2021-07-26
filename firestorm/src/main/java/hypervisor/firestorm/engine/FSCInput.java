package hypervisor.firestorm.engine;

import android.view.MotionEvent;

import hypervisor.firestorm.mesh.FSTypeRender;
import hypervisor.vanguard.list.VLListType;

public final class FSCInput{

    public static final InputHandler TOUCH = new InputHandler();
    public static final InputHandler DOWN = new InputHandler();
    public static final InputHandler SINGLETAP = new InputHandler();
    public static final InputHandler LONGPRESS = new InputHandler();
    public static final InputHandler SHOWPRESS = new InputHandler();
    public static final InputHandler SCROLL = new InputHandler();
    public static final InputHandler FLING = new InputHandler();

    public static final class InputHandler{

        public InputHandler(){

        }

        public final VLListType<TypeProcessor> PRE = new VLListType<>(20, 50);
        public final VLListType<TypeProcessor> POST = new VLListType<>(20, 50);
        private final VLListType<TypeProcessor> CACHE = new VLListType<>(20, 50);
        public final PostTask TASK = new PostTask();
        public final Object LOCK = new Object();

        protected void trigger(MotionEvent e1, MotionEvent e2, float f1, float f2){
            synchronized(LOCK){
                int size = PRE.size();
                int size2 = POST.size();

                Object[] array = PRE.array();
                Object[] array2 = POST.array();

                for(int i = 0; i < size; i++){
                    PRE.add((TypeProcessor)array[i]);
                }
                for(int i = 0; i < size2; i++){
                    POST.add((TypeProcessor)array2[i]);
                }
            }

            TASK.update(CACHE, e1, e2, f1, f2);
            FSR.postTask(TASK);
        }
    }

    public interface TypeProcessor{

        void process(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }

    public static class ProcessorRenderType implements TypeProcessor{

        public FSTypeRender target;

        public ProcessorRenderType(FSTypeRender target){
            this.target = target;
        }

        protected ProcessorRenderType(){

        }

        @Override
        public void process(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
            target.checkInputs(e1, e2, f1, f2, near, far);
        }
    }

    public static class PostTask implements FSRTask{

        public VLListType<TypeProcessor> cache;
        public MotionEvent e1;
        public MotionEvent e2;
        public float f1;
        public float f2;

        public PostTask(){

        }

        public void update(VLListType<TypeProcessor> cache, MotionEvent e1, MotionEvent e2, float f1, float f2){
            this.cache = cache;
            this.e1 = e1;
            this.e2 = e2;
            this.f1 = f1;
            this.f2 = f2;
        }

        @Override
        public void run(){
            int size = cache.size();
            float[] near = new float[4];
            float[] far = new float[4];

            FSView config = FSControl.getView();
            config.unProject2DPoint(e1.getX(), e1.getY(), near, 0, far, 0);

            for(int i = 0; i < size; i++){
                cache.get(i).process(e1, e2, f1, f2, near, far);
            }
        }
    }
}
