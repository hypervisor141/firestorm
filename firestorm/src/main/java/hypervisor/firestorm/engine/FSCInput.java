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

        public final VLListType<TypeProcessor> PRE = new VLListType<>(10, 50);
        public final VLListType<TypeProcessor> POST = new VLListType<>(10, 50);
        private final VLListType<TypeProcessor> CACHE = new VLListType<>(10, 50);
        public final Object LOCK = new Object();

        protected boolean trigger(MotionEvent e1, MotionEvent e2, float f1, float f2){
            synchronized(LOCK){
                int size = PRE.size();
                int size2 = POST.size();

                Object[] array = PRE.array();
                Object[] array2 = POST.array();

                CACHE.clear();

                for(int i = 0; i < size; i++){
                    CACHE.add((TypeProcessor)array[i]);
                }
                for(int i = 0; i < size2; i++){
                    CACHE.add((TypeProcessor)array2[i]);
                }
            }

            float[] near = new float[4];
            float[] far = new float[4];

            FSView config = FSControl.getView();
            config.unProject2DPoint(e1.getX(), e1.getY(), near, 0, far, 0);

            int size = CACHE.size();

            for(int i = 0; i < size; i++){
                if(CACHE.get(i).process(e1, e2, f1, f2, near, far)){
                    return true;
                }
            }

            return false;
        }
    }

    public interface TypeProcessor{

        boolean process(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }

    public static class ProcessorRenderType implements TypeProcessor{

        public FSTypeRender target;

        public ProcessorRenderType(FSTypeRender target){
            this.target = target;
        }

        protected ProcessorRenderType(){

        }

        @Override
        public boolean process(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
            return target.checkInputs(e1, e2, f1, f2, near, far);
        }
    }
}
