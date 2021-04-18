package com.nurverek.firestorm;

import android.view.MotionEvent;

import java.util.ArrayList;

public final class FSCInput{

    private static ArrayList<Entry> LISTENRER_TOUCH;
    private static ArrayList<Entry> LISTENRER_DOWN;
    private static ArrayList<Entry> LISTENRER_SINGLETAP;
    private static ArrayList<Entry> LISTENRER_LONGPRESS;
    private static ArrayList<Entry> LISTENRER_SHOWPRESS;
    private static ArrayList<Entry> LISTENRER_SCROLL;
    private static ArrayList<Entry> LISTENRER_FLING;

    public static final Type TYPE_TOUCH = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LISTENRER_TOUCH;
        }
    };
    public static final Type TYPE_DOWN = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LISTENRER_DOWN;
        }
    };
    public static final Type TYPE_SINGLETAP = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LISTENRER_SINGLETAP;
        }
    };
    public static final Type TYPE_LONGPRESS = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LISTENRER_LONGPRESS;
        }
    };
    public static final Type TYPE_SHOWPRESS = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LISTENRER_SHOWPRESS;
        }
    };
    public static final Type TYPE_SCROLL = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LISTENRER_SCROLL;
        }
    };
    public static final Type TYPE_FLING = new Type(){
        @Override
        public ArrayList<Entry> get(){
            return LISTENRER_FLING;
        }
    };

    private static Listener LISTENER_MAIN;
    private static final CheckInput POST_INPUT_CHECK = new CheckInput();

    public static final int INPUT_CHECK_CONTINUE = 9174;
    public static final int INPUT_CHECK_STOP = 9175;

    private static final float[] NEARCACHE = new float[4];
    private static final float[] FARCACHE = new float[4];

    private static final Object LOCK = new Object();

    protected static void initialize(){
        LISTENER_MAIN = new Listener(){
            @Override
            public void preProcess(){

            }

            @Override
            public void postProcess(){

            }
        };

        LISTENRER_TOUCH = new ArrayList<>();
        LISTENRER_DOWN = new ArrayList<>();
        LISTENRER_SINGLETAP = new ArrayList<>();
        LISTENRER_LONGPRESS = new ArrayList<>();
        LISTENRER_SHOWPRESS = new ArrayList<>();
        LISTENRER_SCROLL = new ArrayList<>();
        LISTENRER_FLING = new ArrayList<>();
    }



    public static void setMainListener(Listener listener){
        if(listener == null){
            throw new RuntimeException("ProcessListener can't be null.");
        }

        synchronized(LOCK){
            LISTENER_MAIN = listener;
        }
    }

    public static void add(Type type, Entry entry){
        synchronized(LOCK){
            type.get().add(entry);
        }
    }

    public static void remove(Type type, int index){
        synchronized(LOCK){
            type.get().remove(index);
        }
    }

    public static Entry get(Type type, int index){
        synchronized(LOCK){
            return type.get().get(index);
        }
    }

    public static ArrayList<Entry> get(Type type){
        synchronized(LOCK){
            return type.get();
        }
    }

    public static int size(Type type){
        synchronized(LOCK){
            return type.get().size();
        }
    }

    public static void clear(Type type){
        synchronized(LOCK){
            type.get().clear();
        }
    }

    protected static void checkInput(Type type, MotionEvent e1, MotionEvent e2, float f1, float f2){
        POST_INPUT_CHECK.type = type;
        POST_INPUT_CHECK.e1 = e1;
        POST_INPUT_CHECK.e2 = e2;
        POST_INPUT_CHECK.f1 = f1;
        POST_INPUT_CHECK.f2 = f2;

        FSR.post(POST_INPUT_CHECK);
    }

    protected static void destroy(){
        if(!FSControl.getKeepAlive()){
            LISTENRER_TOUCH = null;
            LISTENRER_DOWN = null;
            LISTENRER_SINGLETAP = null;
            LISTENRER_LONGPRESS = null;
            LISTENRER_SHOWPRESS = null;
            LISTENRER_SCROLL = null;
            LISTENRER_FLING = null;
            LISTENER_MAIN = null;
        }
    }

    private static final class CheckInput implements FSRTask{

        protected Type type;
        protected MotionEvent e1;
        protected MotionEvent e2;
        protected float f1;
        protected float f2;

        @Override
        public void run(){
            LISTENER_MAIN.preProcess();

            FSView config = FSControl.getView();
            config.unProject2DPoint(e1.getX(), e1.getY(), NEARCACHE, 0, FARCACHE, 0);

            ArrayList<Entry> entries = type.get();
            int size = entries.size();

            for(int i = 0; i < size; i++){
                if(entries.get(i).processInput(type, e1, e2, f1, f2)){
                    break;
                }
            }

            LISTENER_MAIN.postProcess();
        }
    }

    public interface Entry{

        boolean processInput(Type type, MotionEvent e1, MotionEvent e2, float f1, float f2);
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
        public boolean processInput(Type type, MotionEvent e1, MotionEvent e2, float f1, float f2){
            FSBounds.Collision results = mesh.get(instanceindex).schematics().checkInputCollision(NEARCACHE, FARCACHE);
            int status = -1;

            if(results != null){
                status = listener.activated(results, this, results.boundsindex, e1, e2, f1, f2, NEARCACHE, FARCACHE);
            }

            return status == INPUT_CHECK_STOP;
        }
    }

    public static interface Listener{

        void preProcess();
        void postProcess();
    }

    public static interface CollisionListener{

        int activated(FSBounds.Collision results, MeshEntry entry, int boundindex, MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }

    public static abstract class Type{

        public Type(){

        }
        
        protected abstract ArrayList<Entry> get();
    }
}
