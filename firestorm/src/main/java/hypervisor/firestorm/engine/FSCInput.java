package hypervisor.firestorm.engine;

import android.view.MotionEvent;

import java.util.ArrayList;

import hypervisor.firestorm.mesh.FSTypeRender;

public final class FSCInput{

    private static ArrayList<Processor> LIST_TOUCH;
    private static ArrayList<Processor> LIST_DOWN;
    private static ArrayList<Processor> LIST_SINGLETAP;
    private static ArrayList<Processor> LIST_LONGPRESS;
    private static ArrayList<Processor> LIST_SHOWPRESS;
    private static ArrayList<Processor> LIST_SCROLL;
    private static ArrayList<Processor> LIST_FLING;

    public static final InputType TYPE_TOUCH = new InputType(){
        @Override
        public ArrayList<Processor> get(){
            return LIST_TOUCH;
        }
    };
    public static final InputType TYPE_DOWN = new InputType(){
        @Override
        public ArrayList<Processor> get(){
            return LIST_DOWN;
        }
    };
    public static final InputType TYPE_SINGLETAP = new InputType(){
        @Override
        public ArrayList<Processor> get(){
            return LIST_SINGLETAP;
        }
    };
    public static final InputType TYPE_LONGPRESS = new InputType(){
        @Override
        public ArrayList<Processor> get(){
            return LIST_LONGPRESS;
        }
    };
    public static final InputType TYPE_SHOWPRESS = new InputType(){
        @Override
        public ArrayList<Processor> get(){
            return LIST_SHOWPRESS;
        }
    };
    public static final InputType TYPE_SCROLL = new InputType(){
        @Override
        public ArrayList<Processor> get(){
            return LIST_SCROLL;
        }
    };
    public static final InputType TYPE_FLING = new InputType(){
        @Override
        public ArrayList<Processor> get(){
            return LIST_FLING;
        }
    };

    protected static void initialize(){
        LIST_TOUCH = new ArrayList<>();
        LIST_DOWN = new ArrayList<>();
        LIST_SINGLETAP = new ArrayList<>();
        LIST_LONGPRESS = new ArrayList<>();
        LIST_SHOWPRESS = new ArrayList<>();
        LIST_SCROLL = new ArrayList<>();
        LIST_FLING = new ArrayList<>();
    }

    public static void add(InputType type, Processor processor){
        synchronized(type){
            type.get().add(processor);
        }
    }

    public static void remove(InputType type, int index){
        synchronized(type){
            type.get().remove(index);
        }
    }

    public static Processor get(InputType type, int index){
        synchronized(type){
            return type.get().get(index);
        }
    }

    public static ArrayList<Processor> get(InputType type){
        synchronized(type){
            return type.get();
        }
    }

    public static int size(InputType type){
        synchronized(type){
            return type.get().size();
        }
    }

    public static void clear(InputType type){
        synchronized(type){
            type.get().clear();
        }
    }

    public static void triggerInput(InputType type, MotionEvent e1, MotionEvent e2, float f1, float f2){
        FSR.post(new TaskProcessInput(type, e1, e2, f1, f2), false);
    }

    protected static void destroy(boolean destroyonpause){
        if(destroyonpause){
            LIST_TOUCH = null;
            LIST_DOWN = null;
            LIST_SINGLETAP = null;
            LIST_LONGPRESS = null;
            LIST_SHOWPRESS = null;
            LIST_SCROLL = null;
            LIST_FLING = null;
        }
    }

    public static abstract class InputType{

        public InputType(){

        }

        protected abstract ArrayList<Processor> get();
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

    private static final class TaskProcessInput implements FSRTask{

        protected InputType type;
        protected MotionEvent e1;
        protected MotionEvent e2;
        protected float f1;
        protected float f2;

        protected TaskProcessInput(InputType type, MotionEvent e1, MotionEvent e2, float f1, float f2){
            this.type = type;
            this.e1 = e1;
            this.e2 = e2;
            this.f1 = f1;
            this.f2 = f2;
        }

        protected TaskProcessInput(){

        }

        @Override
        public void run(){
            float[] near = new float[4];
            float[] far = new float[4];

            FSView config = FSControl.getView();
            config.unProject2DPoint(e1.getX(), e1.getY(), near, 0, far, 0);

            ArrayList<Processor> cache;

            synchronized(type){
                ArrayList<Processor> entries = type.get();
                cache = new ArrayList<>(entries);
            }

            int size = cache.size();

            for(int i = 0; i < size; i++){
                cache.get(i).process(e1, e2, f1, f2, near, far);
            }
        }
    }
}
