package com.nurverek.firestorm;

import android.view.MotionEvent;

import java.util.ArrayList;

public final class FSCInput{

    private static ArrayList<Entry> LIST_TOUCH;
    private static ArrayList<Entry> LIST_DOWN;
    private static ArrayList<Entry> LIST_SINGLETAP;
    private static ArrayList<Entry> LIST_LONGPRESS;
    private static ArrayList<Entry> LIST_SHOWPRESS;
    private static ArrayList<Entry> LIST_SCROLL;
    private static ArrayList<Entry> LIST_FLING;

    public static final Type TYPE_TOUCH = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LIST_TOUCH;
        }
    };
    public static final Type TYPE_DOWN = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LIST_DOWN;
        }
    };
    public static final Type TYPE_SINGLETAP = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LIST_SINGLETAP;
        }
    };
    public static final Type TYPE_LONGPRESS = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LIST_LONGPRESS;
        }
    };
    public static final Type TYPE_SHOWPRESS = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LIST_SHOWPRESS;
        }
    };
    public static final Type TYPE_SCROLL = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LIST_SCROLL;
        }
    };
    public static final Type TYPE_FLING = new Type(){
        @Override
        public ArrayList<Entry> get(){
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

    public static void add(Type type, Entry entry){
        synchronized(type){
            type.get().add(entry);
        }
    }

    public static void remove(Type type, int index){
        synchronized(type){
            type.get().remove(index);
        }
    }

    public static Entry get(Type type, int index){
        synchronized(type){
            return type.get().get(index);
        }
    }

    public static ArrayList<Entry> get(Type type){
        synchronized(type){
            return type.get();
        }
    }

    public static int size(Type type){
        synchronized(type){
            return type.get().size();
        }
    }

    public static void clear(Type type){
        synchronized(type){
            type.get().clear();
        }
    }

    public static void triggerInput(Type type, MotionEvent e1, MotionEvent e2, float f1, float f2){
        FSR.post(new TaskProcessInput(type, e1, e2, f1, f2));
    }

    protected static void destroy(){
        if(FSControl.getDestroyOnPause()){
            LIST_TOUCH = null;
            LIST_DOWN = null;
            LIST_SINGLETAP = null;
            LIST_LONGPRESS = null;
            LIST_SHOWPRESS = null;
            LIST_SCROLL = null;
            LIST_FLING = null;
        }
    }

    private static final class TaskProcessInput implements FSRTask{

        protected final Type type;
        protected final MotionEvent e1;
        protected final MotionEvent e2;
        protected final float f1;
        protected final float f2;

        protected TaskProcessInput(Type type, MotionEvent e1, MotionEvent e2, float f1, float f2){
            this.type = type;
            this.e1 = e1;
            this.e2 = e2;
            this.f1 = f1;
            this.f2 = f2;
        }

        @Override
        public void run(){
            float[] near = new float[4];
            float[] far = new float[4];

            FSView config = FSControl.getView();
            config.unProject2DPoint(e1.getX(), e1.getY(), near, 0, far, 0);

            ArrayList<Entry> cache;

            synchronized(type){
                ArrayList<Entry> entries = type.get();
                cache = new ArrayList<>(entries);
            }

            int size = cache.size();

            for(int i = 0; i < size; i++){
                cache.get(i).processInput(e1, e2, f1, f2, near, far);
            }
        }
    }

    public interface Entry{

        void processInput(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }

    public static class MeshEntry implements Entry{

        public FSMesh mesh;
        public CollisionListener listener;
        public int instanceindex;

        public MeshEntry(FSMesh mesh, int instanceindex, CollisionListener listener){
            this.mesh = mesh;
            this.instanceindex = instanceindex;
            this.listener = listener;
        }

        @Override
        public void processInput(MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far){
            FSBounds.Collision results = mesh.get(instanceindex).schematics().checkInputCollision(near, far);

            if(results != null){
                listener.activated(results, this, e1, e2, f1, f2, near, far);
            }
        }
    }

    public interface CollisionListener{

        void activated(FSBounds.Collision results, MeshEntry entry, MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }

    public static abstract class Type{

        public Type(){

        }
        
        protected abstract ArrayList<Entry> get();
    }
}
