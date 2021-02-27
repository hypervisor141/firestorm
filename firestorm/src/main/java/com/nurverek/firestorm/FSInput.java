package com.nurverek.firestorm;

import android.view.MotionEvent;

import java.util.ArrayList;

public final class FSInput{

    private static ArrayList<Entry> LISTENRER_TOUCH;
    private static ArrayList<Entry> LISTENRER_DOWN;
    private static ArrayList<Entry> LISTENRER_SINGLETAP;
    private static ArrayList<Entry> LISTENRER_LONGPRESS;
    private static ArrayList<Entry> LISTENRER_SHOWPRESS;
    private static ArrayList<Entry> LISTENRER_SCROLL;
    private static ArrayList<Entry> LISTENRER_FLING;

    private static Listener LISTENER_MAIN;

    public static final int INPUT_CHECK_CONTINUE = 9174;
    public static final int INPUT_CHECK_STOP = 9175;

    private static final float[] NEARCACHE = new float[4];
    private static final float[] FARCACHE = new float[4];

    private static Entry CURRENT_ENTRY;
    private static MotionEvent CURRENT_ME1;
    private static MotionEvent CURRENT_ME2;
    private static float CURRENT_F1;
    private static float CURRENT_F2;
    private static int CURRENT_STATUS;

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

        synchronized(FSRenderer.RENDERLOCK){
            LISTENER_MAIN = listener;
        }
    }

    public static void add(Type type, Entry listener){
        synchronized(FSRenderer.RENDERLOCK){
            type.get().add(listener);
        }
    }

    public static void remove(Type type, int index){
        synchronized(FSRenderer.RENDERLOCK){
            type.get().remove(index);
        }
    }

    public static void removeAllMatching(Type type, long id){
        synchronized(FSRenderer.RENDERLOCK){
            ArrayList<Entry> entries = type.get();
            int size = entries.size();

            for(int i = 0; i < size; i++){
                if(entries.get(i).mesh.id() == id){
                    entries.remove(i);
                    i--;
                }
            }
        }
    }

    public static Entry get(Type type, int index){
        synchronized(FSRenderer.RENDERLOCK){
            return type.get().get(index);
        }
    }

    public static ArrayList<Entry> get(Type type){
        synchronized(FSRenderer.RENDERLOCK){
            return type.get();
        }
    }

    public static int size(Type type){
        synchronized(FSRenderer.RENDERLOCK){
            return type.get().size();
        }
    }

    public static void clear(Type type){
        synchronized(FSRenderer.RENDERLOCK){
            type.get().clear();
        }
    }

    public static void checkInput(Type type, MotionEvent e1, MotionEvent e2, float f1, float f2){
        synchronized(FSRenderer.RENDERLOCK){
            CURRENT_ME1 = e1;
            CURRENT_ME2 = e2;
            CURRENT_F1 = f1;
            CURRENT_F2 = f2;

            FSViewConfig config = FSControl.getViewConfig();
            config.unProject2DPoint(e1.getX(), e1.getY(), NEARCACHE, 0, FARCACHE, 0);

            ArrayList<Entry> entries = type.get();
            int size = entries.size();

            LISTENER_MAIN.preProcess();

            for(int i = 0; i < size; i++){
                CURRENT_ENTRY = entries.get(i);
                CURRENT_ENTRY.mesh.instance(CURRENT_ENTRY.instanceindex).schematics().checkInputCollision(NEARCACHE, FARCACHE);

                if(CURRENT_STATUS == INPUT_CHECK_STOP){
                    break;
                }
            }

            LISTENER_MAIN.postProcess();
        }
    }

    protected static boolean signalCollision(FSBounds.Collision results, int boundsindex){
        CURRENT_STATUS = CURRENT_ENTRY.listener.activated(results, CURRENT_ENTRY, boundsindex, CURRENT_ME1, CURRENT_ME2, CURRENT_F1, CURRENT_F2, NEARCACHE, FARCACHE);
        return CURRENT_STATUS == INPUT_CHECK_STOP;
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

    public static class Entry{

        public FSMesh mesh;
        public CollisionListener listener;
        public int instanceindex;

        public Entry(FSMesh mesh, int instanceindex, CollisionListener listener){
            this.mesh = mesh;
            this.instanceindex = instanceindex;
            this.listener = listener;
        }
    }

    public static interface Listener{

        void preProcess();
        void postProcess();
    }

    public static interface CollisionListener{

        int activated(FSBounds.Collision results, Entry entry, int boundindex, MotionEvent e1, MotionEvent e2, float f1, float f2, float[] near, float[] far);
    }

    protected static abstract class Type{

        public Type(){

        }
        
        protected abstract ArrayList<Entry> get();
    }
}
