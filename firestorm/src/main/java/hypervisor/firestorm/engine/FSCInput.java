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

        public final VLListType<TypeProcessor> PROCESSORS = new VLListType<>(20, 50);

        protected boolean trigger(MotionEvent e1, MotionEvent e2, float f1, float f2){
            float[] near = new float[4];
            float[] far = new float[4];

            FSView config = FSControl.getView();
            config.unProject2DPoint(e1.getX(), e1.getY(), near, 0, far, 0);

            int size = PROCESSORS.size();

            for(int i = 0; i < size; i++){
                if(PROCESSORS.get(i).process(e1, e2, f1, f2, near, far)){
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
