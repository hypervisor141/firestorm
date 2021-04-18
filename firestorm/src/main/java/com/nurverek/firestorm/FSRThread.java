package com.nurverek.firestorm;

import android.os.Looper;

import java.util.ArrayList;

public class FSRThread extends Thread{

    protected static final int CREATE_GL_CONTEXT = 7435;
    protected static final int SURFACE_CREATED = 7436;
    protected static final int SURFACE_CHANGED = 7437;
    protected static final int DRAW_FRAME = 7438;

    private final Object lock;
    private boolean ready;

    private volatile boolean running;

    private final ArrayList<Integer> orders;
    private final ArrayList<Object> data;

    public FSRThread(){
        orders = new ArrayList<>();
        data = new ArrayList<>();

        lock = new Object();
        running = false;
    }

    @Override
    public void run(){
        Looper.prepare();

        synchronized(lock){
            ready = true;
            lock.notify();
        }

        ArrayList<Object> data = new ArrayList<>();
        ArrayList<Integer> orders = new ArrayList<>();

        while(running){
            synchronized(lock){
                while(this.orders.isEmpty() && running){
                    try{
                        lock.wait();
                    }catch(InterruptedException ex){
                        ex.printStackTrace();
                    }
                }

                orders.addAll(this.orders);
                data.addAll(this.data);

                this.orders.clear();
                this.data.clear();
            }

            int size = orders.size();
            int order;
            Object obj;

            for(int i = 0; i < size; i++){
                order = orders.get(i);
                obj = data.get(i);

                if(order == CREATE_GL_CONTEXT){
                    Object[] msg = (Object[])obj;
                    FSCEGL.initialize(FSControl.getSurface().getHolder(), (int[])msg[0], (boolean)msg[1]);

                }else if(order == SURFACE_CREATED){
                    FSR.onSurfaceCreated((boolean)obj);

                }else if(order == SURFACE_CHANGED){
                    int[] a = (int[])obj;
                    FSR.onSurfaceChanged(a[0], a[1]);

                }else if(order == DRAW_FRAME){
                    FSR.onDrawFrame();
                }
            }

            orders.clear();
            data.clear();
        }

        synchronized(lock){
            ready = false;
        }
    }

    protected void initialize(){
        synchronized(lock){
            running = true;
            start();

            while(!ready){
                try{
                    lock.wait();
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean running(){
        return running;
    }

    public Object lock(){
        return lock;
    }

    public FSRThread post(int code, Object d){
        synchronized(lock){
            orders.add(code);
            data.add(d);

            lock.notify();
        }

        return this;
    }

    protected FSRThread shutdown(){
        synchronized(lock){
            running = false;
            lock.notify();
        }

        try{
            join();
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }

        return this;
    }
}
